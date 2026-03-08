package io.github.wifi_password_manager.utils

import android.security.keystore.KeyProperties
import io.github.wifi_password_manager.domain.model.ExportOption
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object Crypto {
    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    private const val ITERATION_COUNT = 100_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    fun encrypt(content: ByteArray, password: String, option: ExportOption): ByteArray {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val encrypted = cipher.doFinal(content)

        return byteArrayOf(option.ordinal.toByte()) + salt + iv + encrypted
    }

    fun decrypt(content: ByteArray, password: String): Pair<ExportOption, ByteArray> {
        val option = ExportOption.entries[content[0].toInt()]
        val salt = content.copyOfRange(1, 1 + SALT_LENGTH)
        val iv = content.copyOfRange(1 + SALT_LENGTH, 1 + SALT_LENGTH + GCM_IV_LENGTH)
        val encrypted = content.copyOfRange(1 + SALT_LENGTH + GCM_IV_LENGTH, content.size)
        val key = deriveKey(password, salt)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        val decrypted = cipher.doFinal(encrypted)

        return option to decrypted
    }
}
