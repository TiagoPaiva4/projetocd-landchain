/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package token;

import rwa.RWARecord;

/**
 *
 * @author Tiago Paiva
 */

public class TokenRegistry {

    public void mintTokensForRwa(RWARecord record) {
        System.out.println("Minting tokens for RWA: " + record.getAssetID());
    }
}
