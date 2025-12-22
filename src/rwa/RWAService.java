package rwa;

import blockchain.Block;
import blockchain.BlockChain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RWAService {
    
    // O service agora não "tem" a blockchain, ele apenas a processa quando pedido
    private String carteiraAtual;

    // ESTADO (Memória)
    private Map<String, ImovelRWA> imoveis = new HashMap<>();
    private Map<String, OrdemVenda> mercado = new HashMap<>();
    private Map<String, Carteira> carteirasRegistadas = new HashMap<>(); 
    private List<String> historicoRendas = new ArrayList<>();

    public RWAService(String nomeCarteira) {
        this.carteiraAtual = nomeCarteira;
    }

    // Método para atualizar o estado lendo uma Blockchain externa
    public void atualizarEstado(BlockChain blockchain) {
        if (blockchain == null) return;

        // Limpar tudo
        imoveis.clear();
        mercado.clear();
        historicoRendas.clear();
        carteirasRegistadas.clear();

        // Ler todos os blocos
        for (Block b : blockchain.getBlocks()) {
            // O teu Block original retorna List<String> ou List<Object>
            List<Object> transacoes = b.getTransactions(); 
            
            for (Object obj : transacoes) {
                // Tenta converter o texto (Base64) para TransacaoRWA
                TransacaoRWA tx = UtilsRWA.textoParaTransacao(obj.toString());
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
                if (tx.dadosImovel != null) imoveis.put(tx.dadosImovel.id, tx.dadosImovel);
                break;
                
            case VALIDAR_RWA:
                if (imoveis.containsKey(tx.idRwaAlvo)) imoveis.get(tx.idRwaAlvo).estado = "VALIDADO";
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
                    historicoRendas.add(String.format("RENDA: %s gerou $%.2f", rwa.nome, tx.valorFinanceiro));
                }
                break;

            case MERCADO_CRIAR_ORDEM:
                if (tx.dadosOrdem != null) mercado.put(tx.dadosOrdem.idOrdem, tx.dadosOrdem);
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

    // GETTERS
    public List<ImovelRWA> getListaImoveis() { return new ArrayList<>(imoveis.values()); }
    public List<String> getHistoricoRendas() { return historicoRendas; }
    public List<OrdemVenda> getMercado() { return new ArrayList<>(mercado.values()); }
    
    public String getMeusSaldos() {
        StringBuilder sb = new StringBuilder();
        if (carteirasRegistadas.containsKey(carteiraAtual)) sb.append("[CONTA REGISTADA]\n\n");
        else sb.append("[CONTA NÃO REGISTADA]\n\n");

        for(ImovelRWA rwa : imoveis.values()) {
            int saldo = rwa.distribuicaoTokens.getOrDefault(carteiraAtual, 0);
            if(saldo > 0) sb.append(String.format("- %s (ID: %s): %d Tokens\n", rwa.nome, rwa.id, saldo));
        }
        return sb.length() == 0 ? sb.toString() + "Sem tokens." : sb.toString();
    }
}