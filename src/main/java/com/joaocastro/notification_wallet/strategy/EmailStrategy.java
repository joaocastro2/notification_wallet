package com.joaocastro.notification_wallet.strategy;

@FunctionalInterface
public interface EmailStrategy<T> {

    /**
     * Envia e-mail formatado de acordo com a estratégia concreta e o dado fornecido.
     *
     * @param data dados da notificação / evento
     */
    void sendEmail(T data);

    /**
     * Método utilitário alternativo para envio de e-mail delegando para sendEmail.
     *
     * @param data dados da notificação / evento
     */
    default void send(T data) {
        sendEmail(data);
    }
}
