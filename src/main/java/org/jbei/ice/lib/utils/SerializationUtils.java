package org.jbei.ice.lib.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.commons.codec.binary.Base64;

public class SerializationUtils {
    public static class SerializationUtilsException extends Exception {
        private static final long serialVersionUID = -6597529889622775652L;

        public SerializationUtilsException() {
            super();
        }

        public SerializationUtilsException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * deserialize to a java object
     * 
     * @param serializedObject
     * @return
     * @throws SerializationUtilsException
     */
    public static Serializable deserializeStringToObject(String serializedObject)
            throws SerializationUtilsException {
        try {

            ObjectInputStream objectInputStream;
            objectInputStream = new ObjectInputStream(new ByteArrayInputStream(
                    deserializeFromStringToByteArray(serializedObject)));
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

    private static byte[] deserializeFromStringToByteArray(String serializedObject) {
        byte[] data = new Base64().decode(serializedObject);
        return data;
    }

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

        return new Base64().encodeToString(byteArrayOutputStream.toByteArray());
    }

    public static String serializeBytesToString(byte[] bytes) {
        return new Base64().encodeToString(bytes);
    }

    public static byte[] deserializeStringToBytes(String data) {
        return new Base64().decode(data);
    }
}
