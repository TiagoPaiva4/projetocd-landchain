package rwa;

import blockchain.Block;
import blockchain.BlockChain;
import blockchain.MinerDistibuted;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RWAService {
    
    private BlockChain blockchain;
    private String carteiraAtual; // Nome do usuário logado nesta sessão

    // ESTADO MUNDIAL (Recalculado sempre a partir da blockchain)
    private Map<String, ImovelRWA> imoveis = new HashMap<>();
    private Map<String, OrdemVenda> mercado = new HashMap<>();
    private Map<String, Carteira> carteirasRegistadas = new HashMap<>(); // Lista de usuários válidos
    private List<String> historicoRendas = new ArrayList<>();

    public RWAService(String nomeCarteira) throws Exception {
        this.carteiraAtual = nomeCarteira;
        
        // CORREÇÃO: Usar try-catch para lidar com a ausência do ficheiro na 1ª execução
        try {
            this.blockchain = BlockChain.load("rwa_chain/chain.bch");
        } catch (Exception e) {
            System.out.println("Blockchain não encontrada. Iniciando uma nova...");
            this.blockchain = null;
        }
        
        // Se não conseguiu carregar (é nulo), cria uma nova com Bloco Genesis
        if (this.blockchain == null) {
            // Cria transação Genesis para registar o SISTEMA
            TransacaoRWA genesisTx = new TransacaoRWA("SISTEMA", TransacaoRWA.Tipo.REGISTAR_CARTEIRA);
            genesisTx.dadosCarteira = new Carteira("SISTEMA");
            
            List<Object> data = new ArrayList<>();
            data.add(genesisTx);
            
            // Cria o bloco e a chain
            Block genesis = new Block(0, new byte[]{0,0,0,0}, 2, data);
            genesis.mine();
            this.blockchain = new BlockChain("rwa_chain/chain.bch", genesis);
        }
        
        // Lê todos os blocos para reconstruir o estado atual
        sincronizarEstado();
    }

    // =================================================================
    // LÓGICA CORE: STATE REPLAY (Ler Blockchain -> Memória)
    // =================================================================
    public void sincronizarEstado() {
        imoveis.clear();
        mercado.clear();
        historicoRendas.clear();
        carteirasRegistadas.clear();

        for (Block b : blockchain.getBlocks()) {
            List transacoes = b.getTransactions(); 
            for (Object obj : transacoes) {
                if (obj instanceof TransacaoRWA) {
                    processarTransacao((TransacaoRWA) obj);
                }
            }
        }
    }

    private void processarTransacao(TransacaoRWA tx) {
        // 1. Processar registo de carteira
        if (tx.tipo == TransacaoRWA.Tipo.REGISTAR_CARTEIRA) {
            if (tx.dadosCarteira != null) {
                carteirasRegistadas.put(tx.dadosCarteira.nome, tx.dadosCarteira);
            }
            return; 
        }

        // 2. Processar outros tipos
        switch (tx.tipo) {
            case REGISTAR_RWA:
                if (tx.dadosImovel != null) {
                    imoveis.put(tx.dadosImovel.id, tx.dadosImovel);
                }
                break;
                
            case VALIDAR_RWA:
                if (imoveis.containsKey(tx.idRwaAlvo)) {
                    imoveis.get(tx.idRwaAlvo).estado = "VALIDADO";
                }
                break;
                
            case TRANSFERIR_TOKENS:
            case MERCADO_CONFIRMAR: // Confirmação de mercado gera transferência
                executarTransferencia(tx.idRwaAlvo, tx.remetente, tx.destinatario, tx.quantidade);
                // Se foi uma venda de mercado, marca a ordem como concluída
                if (tx.tipo == TransacaoRWA.Tipo.MERCADO_CONFIRMAR && mercado.containsKey(tx.dadosOrdem.idOrdem)) {
                     mercado.get(tx.dadosOrdem.idOrdem).estado = "CONCLUIDA";
                }
                break;

            case REGISTAR_RENDA:
                ImovelRWA rwa = imoveis.get(tx.idRwaAlvo);
                if (rwa != null) {
                    String log = String.format("RENDA: Imóvel %s gerou $%.2f", rwa.nome, tx.valorFinanceiro);
                    historicoRendas.add(log);
                }
                break;

            case MERCADO_CRIAR_ORDEM:
                if (tx.dadosOrdem != null) {
                    mercado.put(tx.dadosOrdem.idOrdem, tx.dadosOrdem);
                }
                break;

            case MERCADO_COMPRAR:
                // Atualiza estado da ordem para AGUARDA_CONFIRMACAO
                if (mercado.containsKey(tx.dadosOrdem.idOrdem)) {
                    OrdemVenda ov = mercado.get(tx.dadosOrdem.idOrdem);
                    ov.estado = "AGUARDA_CONFIRMACAO";
                    ov.compradorInteressado = tx.remetente;
                }
                break;
        }
    }

    private void executarTransferencia(String idRwa, String de, String para, int qtd) {
        ImovelRWA rwa = imoveis.get(idRwa);
        if (rwa == null) return;

        Map<String, Integer> dist = rwa.distribuicaoTokens;
        
        int saldoAtual = dist.getOrDefault(de, 0);
        if (saldoAtual >= qtd) {
            dist.put(de, saldoAtual - qtd);
            dist.put(para, dist.getOrDefault(para, 0) + qtd);
        }
    }

    // =================================================================
    // AÇÕES DO USUÁRIO (Mineração Automática)
    // =================================================================
    
    // Método de Segurança: Só permite ações se a carteira existir no Map
    private void verificarRegisto() throws Exception {
        if (!carteirasRegistadas.containsKey(carteiraAtual)) {
            throw new Exception("ACESSO NEGADO: A carteira '" + carteiraAtual + "' não está registada na Blockchain.\nPor favor, registe-se primeiro (Opção 0).");
        }
    }

    // Helper para criar bloco, minerar e salvar
    private void minerarTransacao(TransacaoRWA tx) throws Exception {
        // 1. Criar bloco com a transação
        List<Object> dados = new ArrayList<>();
        dados.add(tx);
        Block novoBloco = blockchain.createNewBlock(dados);
        
        // 2. Minerar (Proof of Work)
        String header = novoBloco.getHeaderDataBase64();
        int dificuldade = novoBloco.getDificulty();
        // Chama o MinerDistibuted original
        int nonce = MinerDistibuted.getNonce(header, dificuldade);
        novoBloco.setNonce(nonce);
        
        // 3. Adicionar à chain
        blockchain.add(novoBloco);
        
        // 4. Atualizar estado local
        sincronizarEstado(); 
    }

    // --- REGISTO DE IDENTIDADE ---
    public void registrarCarteira() throws Exception {
        if (carteirasRegistadas.containsKey(carteiraAtual)) {
            throw new Exception("Esta carteira já existe na rede!");
        }
        TransacaoRWA tx = new TransacaoRWA(carteiraAtual, TransacaoRWA.Tipo.REGISTAR_CARTEIRA);
        tx.dadosCarteira = new Carteira(carteiraAtual);
        minerarTransacao(tx);
    }

    // --- IMÓVEIS ---
    public void registrarRWA(String id, String nome) throws Exception {
        verificarRegisto();
        if (imoveis.containsKey(id)) throw new Exception("ID de imóvel já existe!");
        
        ImovelRWA novoRWA = new ImovelRWA(id, nome, carteiraAtual);
        TransacaoRWA tx = new TransacaoRWA(carteiraAtual, TransacaoRWA.Tipo.REGISTAR_RWA);
        tx.dadosImovel = novoRWA;
        minerarTransacao(tx);
    }
    
    public void validarRWA(String idRwa) throws Exception {
        verificarRegisto();
        TransacaoRWA tx = new TransacaoRWA(carteiraAtual, TransacaoRWA.Tipo.VALIDAR_RWA);
        tx.idRwaAlvo = idRwa;
        minerarTransacao(tx);
    }

    public void transferirTokens(String idRwa, String destino, int qtd) throws Exception {
        verificarRegisto();
        
        // Valida se destino existe
        if (!carteirasRegistadas.containsKey(destino)) {
            throw new Exception("A carteira de destino ("+destino+") não está registada!");
        }

        ImovelRWA rwa = imoveis.get(idRwa);
        if (rwa == null) throw new Exception("RWA não encontrado");
        if (!"VALIDADO".equals(rwa.estado)) throw new Exception("RWA ainda não foi validado!");
        
        int saldo = rwa.distribuicaoTokens.getOrDefault(carteiraAtual, 0);
        if (saldo < qtd) throw new Exception("Saldo insuficiente!");

        TransacaoRWA tx = new TransacaoRWA(carteiraAtual, TransacaoRWA.Tipo.TRANSFERIR_TOKENS);
        tx.idRwaAlvo = idRwa;
        tx.destinatario = destino;
        tx.quantidade = qtd;
        
        minerarTransacao(tx);
    }

    public void registrarRenda(String idRwa, double valor) throws Exception {
         verificarRegisto();
         ImovelRWA rwa = imoveis.get(idRwa);
         if (rwa == null) throw new Exception("RWA não encontrado");

         TransacaoRWA tx = new TransacaoRWA(carteiraAtual, TransacaoRWA.Tipo.REGISTAR_RENDA);
         tx.idRwaAlvo = idRwa;
         tx.valorFinanceiro = valor;
         
         minerarTransacao(tx);
    }
    
    // --- MERCADO ---

    public void criarOrdemVenda(String idRwa, int qtd, double preco) throws Exception {
        verificarRegisto();
        ImovelRWA rwa = imoveis.get(idRwa);
        if (rwa == null || rwa.distribuicaoTokens.getOrDefault(carteiraAtual, 0) < qtd) {
            throw new Exception("Saldo insuficiente para vender.");
        }

        String idOrdem = "ORD-" + System.currentTimeMillis();
        OrdemVenda ordem = new OrdemVenda(idOrdem, carteiraAtual, idRwa, qtd, preco);
        
        TransacaoRWA tx = new TransacaoRWA(carteiraAtual, TransacaoRWA.Tipo.MERCADO_CRIAR_ORDEM);
        tx.dadosOrdem = ordem;
        
        minerarTransacao(tx);
    }

    public void comprarTokens(String idOrdem) throws Exception {
        verificarRegisto();
        OrdemVenda ordem = mercado.get(idOrdem);
        if (ordem == null || !"ABERTA".equals(ordem.estado)) throw new Exception("Ordem indisponível");
        if (ordem.vendedor.equals(carteiraAtual)) throw new Exception("Não pode comprar de si mesmo");

        TransacaoRWA tx = new TransacaoRWA(carteiraAtual, TransacaoRWA.Tipo.MERCADO_COMPRAR);
        tx.dadosOrdem = ordem; 
        
        minerarTransacao(tx);
    }

    public void confirmarVenda(String idOrdem) throws Exception {
        verificarRegisto();
        OrdemVenda ordem = mercado.get(idOrdem);
        if (ordem == null) throw new Exception("Ordem não encontrada");
        if (!ordem.vendedor.equals(carteiraAtual)) throw new Exception("Apenas o vendedor pode confirmar");
        if (!"AGUARDA_CONFIRMACAO".equals(ordem.estado)) throw new Exception("Nenhum comprador aguardando");

        // Esta transação efetiva a troca de posse
        TransacaoRWA tx = new TransacaoRWA(carteiraAtual, TransacaoRWA.Tipo.MERCADO_CONFIRMAR);
        tx.idRwaAlvo = ordem.idRWA;
        tx.remetente = ordem.vendedor; // Sai do vendedor
        tx.destinatario = ordem.compradorInteressado; // Vai para o comprador
        tx.quantidade = ordem.quantidadeTokens;
        tx.dadosOrdem = ordem; // Para fechar a ordem

        minerarTransacao(tx);
    }

    // GETTERS E HELPERS
    public List<ImovelRWA> getListaImoveis() { return new ArrayList<>(imoveis.values()); }
    public List<String> getHistoricoRendas() { return historicoRendas; }
    public List<OrdemVenda> getMercado() { return new ArrayList<>(mercado.values()); }
    public BlockChain getBlockchain() { return blockchain; }
    public boolean isRegistada() { return carteirasRegistadas.containsKey(carteiraAtual); }
    
    public String getMeusSaldos() {
        StringBuilder sb = new StringBuilder();
        if (isRegistada()) sb.append("STATUS: CONTA REGISTADA NA BLOCKCHAIN\n\n");
        else sb.append("STATUS: NÃO REGISTADO (Use a opção 0)\n\n");

        for(ImovelRWA rwa : imoveis.values()) {
            int saldo = rwa.distribuicaoTokens.getOrDefault(carteiraAtual, 0);
            if(saldo > 0) {
                sb.append(String.format("Imóvel: %s (%s) | Tokens: %d\n", rwa.nome, rwa.id, saldo));
            }
        }
        return sb.length() == 0 ? sb.append("Sem tokens.").toString() : sb.toString();
    }
}