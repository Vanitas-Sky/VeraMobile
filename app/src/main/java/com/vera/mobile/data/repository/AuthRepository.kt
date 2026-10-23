package com.vera.mobile.data.repository

import com.vera.mobile.data.local.SessionManager
import com.vera.mobile.data.remote.ApiService
import com.vera.mobile.data.remote.ForgotPasswordRequest
import com.vera.mobile.data.remote.GenericApiResponse
import com.vera.mobile.data.remote.LoginRequest
import com.vera.mobile.data.remote.LoginResponse
import com.vera.mobile.data.remote.RegisterRequest
import com.vera.mobile.data.remote.ResetPasswordRequest
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject
import retrofit2.Response

class AuthRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    /**
     * Inicia sesión, extrae las credenciales y guarda el rol/auditor en SessionManager.
     */
    suspend fun login(email: String, pass: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email = email, password = pass))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                
                val token = body.token
                val userRole = body.user?.role
                val isAuditor = body.user?.isAuditor ?: (userRole == "AUDITOR_EXTERNO")
                val termsAccepted = body.user?.termsAccepted ?: false

                // Solo guardamos la sesión en DataStore si ya aceptó los términos.
                // Si no los ha aceptado, el modal en LoginScreen solicitará la aceptación
                // antes de persistir la sesión.
                if (termsAccepted) {
                    sessionManager.saveSession(
                        token = token,
                        role = userRole,
                        isAuditorUser = isAuditor
                    )
                }

                Result.success(body)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "Credenciales incorrectas o servidor no disponible."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage ?: "Revisa tu red"}"))
        }
    }

    /**
     * Registra una nueva cuenta de usuario.
     */
    suspend fun register(
        name: String,
        email: String,
        pass: String,
        confirmPass: String,
        terms: Boolean
    ): Result<LoginResponse> {
        return try {
            val req = RegisterRequest(
                name = name,
                email = email,
                password = pass,
                passwordConfirmation = confirmPass,
                terms = terms
            )
            val response = apiService.register(req)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                sessionManager.saveSession(
                    token = body.token,
                    role = body.user?.role,
                    isAuditorUser = body.user?.isAuditor ?: false
                )
                Result.success(body)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "No se pudo registrar la cuenta."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage ?: "Revisa tu red"}"))
        }
    }

    /**
     * Solicita el envío del correo o código de recuperación de contraseña.
     */
    suspend fun forgotPassword(email: String): Result<GenericApiResponse> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "No se pudo procesar la solicitud de recuperación."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al conectar con el servidor: ${e.localizedMessage}"))
        }
    }

    /**
     * Restablece la contraseña utilizando el token/código recibido.
     */
    suspend fun resetPassword(
        email: String,
        token: String,
        newPass: String,
        confirmPass: String
    ): Result<GenericApiResponse> {
        return try {
            val request = ResetPasswordRequest(
                email = email,
                token = token,
                password = newPass,
                passwordConfirmation = confirmPass
            )
            val response = apiService.resetPassword(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                    ?: "El token de recuperación es inválido o ha expirado."
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al restablecer contraseña: ${e.localizedMessage}"))
        }
    }

    /**
     * Cierra la sesión en el servidor y limpia DataStore en el dispositivo.
     */
    suspend fun logout(): Result<Unit> {
        return try {
            val token = sessionManager.authToken.firstOrNull()
            if (!token.isNullOrBlank()) {
                apiService.logout("Bearer $token")
            }
            sessionManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            sessionManager.clearSession()
            Result.success(Unit)
        }
    }

    private fun parseErrorMessage(errorJson: String?): String? {
        if (errorJson.isNullOrBlank()) return null
        return try {
            val json = JSONObject(errorJson)
            json.optString("message").takeIf { it.isNotBlank() }
                ?: json.optString("error").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
