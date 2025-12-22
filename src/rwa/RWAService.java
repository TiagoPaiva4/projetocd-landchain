package rwa;

import blockchain.Block;
import blockchain.BlockChain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RWAService {
    
    private BlockChain blockchain;
    private String carteiraAtual; // Nome do utilizador local

    // ESTADO MUNDIAL (Recalculado a partir da blockchain)
    private Map<String, ImovelRWA> imoveis = new HashMap<>();
    private Map<String, OrdemVenda> mercado = new HashMap<>();
    private Map<String, Carteira> carteirasRegistadas = new HashMap<>(); 
    private List<String> historicoRendas = new ArrayList<>();

    public RWAService(String nomeCarteira) {
        this.carteiraAtual = nomeCarteira;
        
        // Tenta carregar a blockchain. Se falhar, inicia com null (o Nó tratará de criar/sincronizar)
        try {
            this.blockchain = BlockChain.load("rwa_chain/chain.bch");
        } catch (Exception e) {
            System.out.println("Blockchain local não encontrada ou erro de leitura.");
            this.blockchain = null;
        }
        
        // Se a blockchain existir, reconstrói o estado imediatamente
        if (this.blockchain != null) {
            sincronizarEstado();
        }
    }

    // =================================================================
    // CORE: STATE REPLAY (Ler Strings Base64 -> Objetos -> Estado)
    // =================================================================
    public void sincronizarEstado() {
        if (blockchain == null) return;

        // Limpar estado atual para reconstruir do zero
        imoveis.clear();
        mercado.clear();
        historicoRendas.clear();
        carteirasRegistadas.clear();

        // Percorrer todos os blocos
        for (Block b : blockchain.getBlocks()) {
            // No sistema original, getTransactions retorna uma lista de Strings/Objects
            List<Object> transacoesRaw = b.getTransactions(); 
            
            for (Object obj : transacoesRaw) {
                String txStr = obj.toString(); // Garante que temos a String Base64
                
                // Converte Base64 -> Objeto TransacaoRWA
                TransacaoRWA tx = UtilsRWA.textoParaTransacao(txStr);
                
                if (tx != null) {
                    processarTransacao(tx);
                }
            }
        }
    }

    private void processarTransacao(TransacaoRWA tx) {
        // 1. Registar Carteira
        if (tx.tipo == TransacaoRWA.Tipo.REGISTAR_CARTEIRA) {
            if (tx.dadosCarteira != null) {
                carteirasRegistadas.put(tx.dadosCarteira.nome, tx.dadosCarteira);
            }
            return; 
        }

        // 2. Outras Transações
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
            case MERCADO_CONFIRMAR: 
                executarTransferencia(tx.idRwaAlvo, tx.remetente, tx.destinatario, tx.quantidade);
                if (tx.tipo == TransacaoRWA.Tipo.MERCADO_CONFIRMAR && mercado.containsKey(tx.dadosOrdem.idOrdem)) {
                     mercado.get(tx.dadosOrdem.idOrdem).estado = "CONCLUIDA";
                }
                break;

            case REGISTAR_RENDA:
                ImovelRWA rwa = imoveis.get(tx.idRwaAlvo);
                if (rwa != null) {
                    historicoRendas.add(String.format("RENDA: %s gerou $%.2f (Data: %d)", 
                            rwa.nome, tx.valorFinanceiro, tx.timestamp));
                }
                break;

            case MERCADO_CRIAR_ORDEM:
                if (tx.dadosOrdem != null) {
                    mercado.put(tx.dadosOrdem.idOrdem, tx.dadosOrdem);
                }
                break;

            case MERCADO_COMPRAR:
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
    // GETTERS E HELPERS
    // =================================================================
    
    // Método chamado pelo Nó Remoto para injetar uma nova blockchain recebida da rede
    public void setBlockchain(BlockChain b) {
        this.blockchain = b;
        sincronizarEstado();
    }
    
    public BlockChain getBlockchain() { return blockchain; }

    public List<ImovelRWA> getListaImoveis() { return new ArrayList<>(imoveis.values()); }
    public List<String> getHistoricoRendas() { return historicoRendas; }
    public List<OrdemVenda> getMercado() { return new ArrayList<>(mercado.values()); }
    public boolean isRegistada() { return carteirasRegistadas.containsKey(carteiraAtual); }
    
    public String getMeusSaldos() {
        StringBuilder sb = new StringBuilder();
        sb.append("Estado da Conta: ").append(isRegistada() ? "REGISTADA" : "NÃO REGISTADA").append("\n\n");
        
        boolean temAlgo = false;
        for(ImovelRWA rwa : imoveis.values()) {
            int saldo = rwa.distribuicaoTokens.getOrDefault(carteiraAtual, 0);
            if(saldo > 0) {
                sb.append(String.format("- %s (ID: %s): %d Tokens\n", rwa.nome, rwa.id, saldo));
                temAlgo = true;
            }
        }
        if(!temAlgo) sb.append("Sem tokens em carteira.");
        return sb.toString();
    }
}