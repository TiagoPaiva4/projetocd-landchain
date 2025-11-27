//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
//::     I N S T I T U T O    P O L I T E C N I C O   D E   T O M A R        ::
//::     Escola Superior de Tecnologia de Tomar                              ::
//::     e-mail: manso@ipt.pt                                                ::
//::     url   : http://orion.ipt.pt/~manso                                  ::
//::                                                                         ::
//::     This software was build with the purpose of investigate and         ::
//::     learning.                                                           ::
//::                                                                         ::
//::                                                               (c)2025   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////

package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Created on 08/10/2025, 14:53:50
 *
 * @author manso - computer
 */
public class Utils {
    
     /**
     * concatenate two byte arrays
     *
     * @param array1 first byte array
     * @param array2 second byte array
     * @return array1 + array2
     */
    public static byte[] concatenate(byte[] array1, byte[] array2) {
        byte[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
      
    
    /**
     * Converts a double value into an array of 8 bytes.
     * Uses a ByteBuffer to handle the binary conversion safely.
     *
     * @param value The double value to convert.
     * @return A byte array representing the double value.
     */
    public static byte[] doubleToBytes(double value) {
        return ByteBuffer.allocate(8).putDouble(value).array();
    }
    
      /**
     * Converts a double value into an array of 8 bytes.
     * Uses a ByteBuffer to handle the binary conversion safely.
     *
     * @param value The double value to convert.
     * @return A byte array representing the double value.
     */
    public static byte[] longToBytes(long value) {
        return ByteBuffer.allocate(8).putLong(value).array();
    }
    

    /**
     * Converts an array of 8 bytes back into a double value.
     * The byte order (endianness) must match the one used during conversion.
     *
     * @param bytes The byte array to convert (must be 8 bytes long).
     * @return The reconstructed double value.
     */
    public static double bytesToDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    public static Serializable fromBase64(String base64) {
        return fromBase64List(base64)[0];
    }

    // Converte uma String Base64 de volta ao objeto original
    public static Serializable[] fromBase64List(String base64) {
        byte[] data = Base64.getDecoder().decode(base64);

        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            List objList = new ArrayList();
            while (ois.available() > 0) {
                objList.add(ois.readObject());
            }
            return (Serializable[]) objList.toArray();
        } catch (Exception e) {
            return null;
        }
    }

    public static String toBase64(List objects) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            for (Object serializable : objects) {
                oos.writeObject(serializable);
            }
            oos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public static String toBase64(Object... objects) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            for (Object obj : objects) {
                oos.writeObject(obj);
            }
            oos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * converts an object to byte array
     *
     * @param object
     * @return byte array
     */
    public static byte[] toBytes(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(object);
            return bos.toByteArray();
        } catch (Exception e) { // not seralizable
            return null;
        }
    }

    /**
     * converts an byte array to byte Array
     *
     * @param data
     * @return byte array
     */
    public static Object toObject(byte[] data) {
        try (ByteArrayInputStream bin = new ByteArrayInputStream(data); ObjectInputStream in = new ObjectInputStream(bin)) {
            return in.readObject();
        } catch (Exception e) { // not seralizable
            return null;
        }
    }

   

    /**
     * Converte um array de bytes para base 64
     *
     * @param data array de bytes
     * @param size tamanho máximo da string
     * @return
     */
    public static String toString(byte[] data, int size) {
        String b64 = Base64.getEncoder().encodeToString(data);
        if (b64.length() < size || size < 4) {
            return b64;
        }
        return b64.substring(0, size - 3) + "...";
    }
    /**
     * Converte um array de bytes para base 64
     *
     * @param data array de bytes
     * @param size tamanho máximo da string
     * @return
     */
    public static String toString(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Converte um array de bytes para base 64
     *
     * @param data array de bytes
     * @param size tamanho máximo da string
     * @return
     */
    public static String toStringList(List<byte[]> data, int size) {
        StringBuilder txt = new StringBuilder();
        for (byte[] array : data) {
            txt.append(toString(array, size)).append(",");
        }
        return txt.substring(0, txt.length() - 1);
    }


    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510081453L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::


///////////////////////////////////////////////////////////////////////////
}
