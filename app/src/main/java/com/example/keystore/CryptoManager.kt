package com.example.keystore

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoManager {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private val encryptCipher get() = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }

    private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry("secret", null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    "secret",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(id: Int, bytes: ByteArray, outputStream: OutputStream) {
        val cipher = encryptCipher
        val encryptedBytes = cipher.doFinal(bytes)
        outputStream.use {
            it.write(id)
            it.write(cipher.iv.size)
            it.write(cipher.iv)
            it.write(encryptedBytes.size shr 8)
            it.write(encryptedBytes.size and 0xFF)
            it.write(encryptedBytes)
        }
    }

    fun decrypt(inputStream: InputStream, targetId: Int): ByteArray? {
        inputStream.use {
            while (it.available() > 0) {
                val id = it.read()
                val ivSize = it.read()
                val iv = ByteArray(ivSize)
                it.read(iv, 0, ivSize)

                val encryptedBytesSize = (it.read() shl 8) or it.read()
                val encryptedBytes = ByteArray(encryptedBytesSize)
                it.read(encryptedBytes, 0, encryptedBytesSize)

                if (id == targetId) {
                    return getDecryptCipherForIv(iv).doFinal(encryptedBytes)
                }
            }
        }
        return null
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}