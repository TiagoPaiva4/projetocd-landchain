/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package network;

import java.io.Serializable;
/**
 *
 * @author Tiago Paiva
 */
public class Message implements Serializable {
    public enum Type {
        NEW_BLOCK,
        NEW_TRANSACTION,
        REQUEST_CHAIN,
        SEND_CHAIN
    }

    public Type type;
    public Object data; // Pode ser um Block, uma Transaction ou uma ArrayList<Block>

    public Message(Type type, Object data) {
        this.type = type;
        this.data = data;
    }
}