package com.vera.mobile.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vera.mobile.data.remote.ApiService
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun TermsAcceptanceModal(
    token: String,
    apiService: ApiService,
    baseUrl: String = "https://vera.sarl",
    onAccepted: () -> Unit,
    onDeclined: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var checkedState by remember { mutableStateOf(false) }

    // Control de pantallas de lectura interna
    var viewingDocument by remember { mutableStateOf<String?>(null) } // "terms" o "privacy"

    Dialog(
        onDismissRequest = { /* Modal obligatorio */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Términos Legales y Privacidad",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Para continuar utilizando Vera Pocket en tu dispositivo, es necesario revisar y autorizar nuestros Términos de Servicio y el Aviso de Privacidad Integral bajo la normativa mexicana (LFPDPPP).",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 18.sp
                )

                // Botones que abren las pantallas internas de lectura
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { viewingDocument = "terms" },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Ver Términos", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    TextButton(
                        onClick = { viewingDocument = "privacy" },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Ver Aviso de Privacidad", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Checkbox de aceptación explícita
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = checkedState,
                        onCheckedChange = { checkedState = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF10B981),
                            uncheckedColor = Color(0xFF64748B)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "He leído y acepto los Términos de Servicio y el Aviso de Privacidad.",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }

                // Botones de acción inferior
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDeclined,
                        enabled = !isSubmitting
                    ) {
                        Text("Rechazar y Salir", color = Color(0xFFF87171), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (checkedState) {
                                isSubmitting = true
                                coroutineScope.launch {
                                    try {
                                        val bearerHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                                        val res = apiService.acceptTerms(bearerHeader)

                                        if (res.isSuccessful && res.body()?.success == true) {
                                            Toast.makeText(context, "Consentimiento guardado", Toast.LENGTH_SHORT).show()
                                            onAccepted()
                                        } else {
                                            val errStr = res.errorBody()?.string()
                                            val msg = try {
                                                JSONObject(errStr ?: "").getString("message")
                                            } catch (e: Exception) {
                                                "Error ${res.code()}: No autorizado"
                                            }
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isSubmitting = false
                                    }
                                }
                            }
                        },
                        enabled = checkedState && !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Aceptar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal de lectura nativa de Términos
    if (viewingDocument == "terms") {
        LegalViewerDialog(
            title = "Términos y Condiciones de Servicio",
            content = """
                1. IDENTIDAD Y ALCANCE
                El presente contrato regula el acceso a Vera ERP y la app móvil Vera Pocket. Al utilizar estas plataformas, el Cliente acepta sujetarse a estos términos.

                2. LICENCIA DE USO
                Se otorga una licencia de uso limitada, no exclusiva, intransferible y revocable bajo modalidad SaaS. El software y su código son propiedad exclusiva de los desarrolladores.

                3. RESTRICCIONES
                Queda expresamente prohibido decompilar, realizar ingeniería inversa o alterar las comunicaciones de la API con fines ilícitos o de extracción no autorizada.

                4. EXCLUSIÓN DE ASESORÍA FISCAL (VERA AI)
                Los reportes generados por la Inteligencia Artificial (Vera AI) constituyen estimaciones de apoyo operativo y no reemplazan la validación de un Contador Público Certificado o dictamen oficial ante el SAT.

                5. LEGISLACIÓN APLICABLE
                Cualquier controversia se regirá bajo las leyes federales de los Estados Unidos Mexicanos y los tribunales competentes correspondientes.
            """.trimIndent(),
            onDismiss = { viewingDocument = null }
        )
    }

    // Modal de lectura nativa de Privacidad
    if (viewingDocument == "privacy") {
        LegalViewerDialog(
            title = "Aviso de Privacidad Integral",
            content = """
                RESPONSABLE DEL TRATAMIENTO
                Vera ERP, con domicilio en México, es responsable del resguardo de sus datos conforme a la nueva Ley Federal de Protección de Datos Personales en Posesión de los Particulares (LFPDPPP).

                DATOS RECABADOS
                - Identificación y Acceso: Correo institucional, nombre, rol y credenciales.
                - Fiscales: RFC, razones sociales, folios fiscales y comprobantes CFDI 4.0.
                - Registro de Consentimiento: Dirección IP, fecha y hora de aceptación.

                FINALIDADES PRIMARIAS
                - Autenticación segura mediante tokens Sanctum.
                - Timbrado y conciliación de facturas electrónicas.
                - Análisis contable asistido y visualización de reportes OpEx.

                DERECHOS ARCO
                Usted puede solicitar el Acceso, Rectificación, Cancelación u Oposición de sus datos escribiendo a privacidad@vera.ai. Cualquier controversia no resuelta podrá ser presentada ante la Secretaría Anticorrupción y Buen Gobierno.
            """.trimIndent(),
            onDismiss = { viewingDocument = null }
        )
    }
}
