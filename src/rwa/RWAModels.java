package rwa;

import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import utils.Serializer;

// ==========================================
// MODELO DE CARTEIRA (IDENTIDADE)
// ==========================================
class Carteira implements Serializable {
    public String nome;
    public long dataRegisto;

    public Carteira(String nome) {
        this.nome = nome;
        this.dataRegisto = System.currentTimeMillis();
    }
    
    @Override
    public String toString() {
        return "Carteira: " + nome + " (Registo: " + new java.util.Date(dataRegisto) + ")";
    }
}

// ==========================================
// MODELO DO IMÓVEL (RWA)
// ==========================================
class ImovelRWA implements Serializable {
    public String id;
    public String nome;
    public String criador; // Carteira que registou
    public String estado; // "PENDENTE" ou "VALIDADO"
    public final int TOTAL_TOKENS = 1000;
    
    // Distribuição atual: Carteira -> Quantidade de Tokens
    // Nota: Este mapa é reconstruído lendo a blockchain.
    public Map<String, Integer> distribuicaoTokens = new HashMap<>();

    public ImovelRWA(String id, String nome, String criador) {
        this.id = id;
        this.nome = nome;
        this.criador = criador;
        this.estado = "PENDENTE";
        // Inicialmente, todos os tokens são do criador
        this.distribuicaoTokens.put(criador, TOTAL_TOKENS);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s | Dono Inicial: %s | Status: %s", id, nome, criador, estado);
    }
}

// ==========================================
// MODELO DE ORDEM DE MERCADO
// ==========================================
class OrdemVenda implements Serializable {
    public String idOrdem;
    public String vendedor;
    public String idRWA;
    public int quantidadeTokens;
    public double precoPorToken;
    public String compradorInteressado; // Quem reservou/quer comprar
    public String estado; // "ABERTA", "AGUARDA_CONFIRMACAO", "CONCLUIDA"

    public OrdemVenda(String idOrdem, String vendedor, String idRWA, int qtd, double preco) {
        this.idOrdem = idOrdem;
        this.vendedor = vendedor;
        this.idRWA = idRWA;
        this.quantidadeTokens = qtd;
        this.precoPorToken = preco;
        this.estado = "ABERTA";
    }
    
    @Override
    public String toString() {
        return String.format("Ordem %s: %d tokens de %s a $%.2f (Vendedor: %s) [%s]", 
                idOrdem, quantidadeTokens, idRWA, precoPorToken, vendedor, estado);
    }
}

// ==========================================
// TRANSAÇÃO GENÉRICA RWA
// ==========================================
class TransacaoRWA implements Serializable {
    public enum Tipo {
        REGISTAR_CARTEIRA,   // Novo tipo para identidade
        REGISTAR_RWA,
        VALIDAR_RWA,
        TRANSFERIR_TOKENS,
        REGISTAR_RENDA,
        MERCADO_CRIAR_ORDEM,
        MERCADO_COMPRAR,     // Comprador manifesta interesse/reserva
        MERCADO_CONFIRMAR    // Vendedor finaliza e tokens movem
    }

    public String idTransacao;
    public long timestamp;
    public String remetente; // Quem iniciou a ação
    public Tipo tipo;
    
    // Dados flexíveis dependendo do tipo
    public Carteira dadosCarteira; // Dados da nova carteira
    public ImovelRWA dadosImovel;
    public OrdemVenda dadosOrdem;
    public String idRwaAlvo;
    public String destinatario;
    public int quantidade;
    public double valorFinanceiro; // Para rendas ou preço total

    public TransacaoRWA(String remetente, Tipo tipo) {
        this.remetente = remetente;
        this.tipo = tipo;
        this.timestamp = System.currentTimeMillis();
        this.idTransacao = UtilsRWA.gerarHash(remetente + timestamp);
    }

    @Override
    public String toString() {
        return String.format("%s | %s | Por: %s", 
                new java.util.Date(timestamp), tipo, remetente);
    }
}


class UtilsRWA {
    
    public static String gerarHash(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) { return "" + System.currentTimeMillis(); }
    }

    // [NOVO] Converte TransacaoRWA para String Base64
    public static String transacaoParaTexto(TransacaoRWA tx) {
        try {
            byte[] bytes = Serializer.objectToByteArray(tx);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // [NOVO] Converte String Base64 de volta para TransacaoRWA
    public static TransacaoRWA textoParaTransacao(String texto) {
        try {
            byte[] bytes = Base64.getDecoder().decode(texto);
            Object obj = Serializer.byteArrayToObject(bytes);
            if (obj instanceof TransacaoRWA) {
                return (TransacaoRWA) obj;
            }
        } catch (Exception e) {
            // Se falhar, é porque não é uma transação RWA (pode ser texto normal)
        }
        return null;
    }
}