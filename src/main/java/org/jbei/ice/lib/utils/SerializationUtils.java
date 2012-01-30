package org.jbei.ice.lib.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;

/**
 * Utility methods for serialization/deserialization.
 * 
 * @author Zinovii Dmytriv, Timothy Ham
 * 
 */
public class SerializationUtils {
    /**
     * Exception class for SerializationUtils.
     * 
     * @author Zinovii Dmytriv
     * 
     */
    public static class SerializationUtilsException extends Exception {
        private static final long serialVersionUID = -6597529889622775652L;

        /**
         * Default constructor.
         */
        public SerializationUtilsException() {
            super();
        }

        /**
         * Constructor using message and cause.
         * 
         * @param message
         *            Message.
         * @param cause
         *            Throwable.
         */
        public SerializationUtilsException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Deserialize Base64 encoded string to a java object.
     * 
     * @param serializedObject
     *            Base64 encoded string of a java object.
     * @return Deserialized java object.
     * @throws SerializationUtilsException
     */
    public static Serializable deserializeStringToObject(String serializedObject)
            throws SerializationUtilsException {
        try {

            ObjectInputStream objectInputStream;
            objectInputStream = new ObjectInputStream(new ByteArrayInputStream(
                    deserializeBase64StringToBytes(serializedObject)));
            Serializable result = (Serializable) objectInputStream.readObject();
            objectInputStream.close();

            return result;
        } catch (IOException e) {
            throw new SerializationUtilsException("Deserialization failed! IOException", e);
        } catch (ClassNotFoundException e) {
            throw new SerializationUtilsException("Deserialization failed! ClassNotFoundException",
                    e);
        }
    }

    /**
     * Serialize java {@link Serializable} object into base64 encoded string.
     * 
     * @param object
     *            Object to serialize.
     * @return Base64 encoded string of object.
     * @throws SerializationUtilsException
     */
    public static String serializeObjectToString(Serializable object)
            throws SerializationUtilsException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
        } catch (IOException e) {
            throw new SerializationUtilsException("Serialization failed! IOException", e);
        }

        return serializeBytesToBase64String(byteArrayOutputStream.toByteArray());
    }

    /**
     * Convert the given byte array into base64 encoded string.
     * 
     * @param bytes
     *            bytes to encdode.
     * @return Base64 encoded string
     */
    public static String serializeBytesToBase64String(byte[] bytes) {
        return new Base64().encodeToString(bytes);
    }

    /**
     * Convert the given base64 encoded string into byte array.
     * 
     * @param base64String
     *            base64 encoded string.
     * @return Byte array
     */
    public static byte[] deserializeBase64StringToBytes(String base64String) {
        return new Base64().decode(base64String);
    }
}
