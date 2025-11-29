package events;

import core.BlockChain;

public class RentListener implements EventListener {

    private final BlockChain blockchain;

    public RentListener(BlockChain blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof RentDistributionEvent e) {
            try {
                // adiciona o evento como dado do bloco
                blockchain.add(new Object[]{e});
                System.out.println("✔ Bloco de renda adicionado: " + e.getAmount() + " €");
            } catch (Exception ex) {
                // regista o erro sem lançar: evita quebra do flow de eventos
                System.err.println("Erro ao adicionar renda à blockchain: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
