import * as SibApiV3Sdk from "@getbrevo/brevo";

const SENDER_EMAIL = "noreply@migxchat.net";
const SENDER_NAME = "Mig33Reborn";

function getBrevoClient() {
  const apiInstance = new SibApiV3Sdk.TransactionalEmailsApi();
  const apiKey = apiInstance.authentications["apiKey"] as { apiKey: string };
  apiKey.apiKey = process.env.BREVO_API_KEY || "";
  return apiInstance;
}

export async function sendVerificationEmail(
  toEmail: string,
  toName: string,
  verifyUrl: string
): Promise<void> {
  if (!process.env.BREVO_API_KEY) {
    console.warn("[Email] BREVO_API_KEY not set. Skipping email send.");
    console.log(`[Email] Verification URL for ${toEmail}: ${verifyUrl}`);
    return;
  }

  const apiInstance = getBrevoClient();

  const sendSmtpEmail = new SibApiV3Sdk.SendSmtpEmail();
  sendSmtpEmail.sender = { name: SENDER_NAME, email: SENDER_EMAIL };
  sendSmtpEmail.to = [{ email: toEmail, name: toName }];
  sendSmtpEmail.subject = "Verifikasi Akun Mig33reborn Kamu";
  sendSmtpEmail.htmlContent = `
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Verifikasi Akun Mig33Reborn</title>
</head>
<body style="margin:0;padding:0;background:#0f172a;font-family:Arial,sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background:#0f172a;padding:40px 0;">
    <tr>
      <td align="center">
        <table width="520" cellpadding="0" cellspacing="0" style="background:#1e293b;border-radius:12px;overflow:hidden;border:1px solid #334155;">
          <tr>
            <td style="background:linear-gradient(135deg,#6366f1,#8b5cf6);padding:32px;text-align:center;">
              <h1 style="color:#fff;margin:0;font-size:28px;font-weight:700;letter-spacing:-0.5px;">MigxChat</h1>
              <p style="color:#c4b5fd;margin:6px 0 0;font-size:14px;">Mig33 Chat Platform</p>
            </td>
          </tr>
          <tr>
            <td style="padding:36px 40px;">
              <h2 style="color:#f1f5f9;margin:0 0 12px;font-size:22px;">Halo, ${toName}! 👋</h2>
              <p style="color:#94a3b8;font-size:15px;line-height:1.6;margin:0 0 24px;">
                Terima kasih sudah mendaftar di <strong style="color:#a78bfa;">Mig33Reborn</strong>.
                Untuk mengaktifkan akun kamu, klik tombol verifikasi di bawah ini.
              </p>
              <div style="text-align:center;margin:32px 0;">
                <a href="${verifyUrl}"
                   style="background:linear-gradient(135deg,#6366f1,#8b5cf6);color:#fff;text-decoration:none;padding:14px 36px;border-radius:8px;font-size:16px;font-weight:600;display:inline-block;">
                  ✅ Verifikasi Akun
                </a>
              </div>
              <p style="color:#64748b;font-size:13px;line-height:1.6;margin:0 0 8px;">
                Link ini berlaku selama <strong style="color:#94a3b8;">1
              
                 jam</strong>.
                Jika kamu tidak mendaftar di MigxChat, abaikan email ini.
              </p>
              <p style="color:#475569;font-size:12px;margin:16px 0 0;word-break:break-all;">
                Atau copy link: <a href="${verifyUrl}" style="color:#818cf8;">${verifyUrl}</a>
              </p>
            </td>
          </tr>
          <tr>
            <td style="background:#0f172a;padding:20px 40px;text-align:center;border-top:1px solid #1e293b;">
              <p style="color:#475569;font-size:12px;margin:0;">
                © 2024 MigxChat · noreply@migxchat.net
              </p>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</body>
</html>
  `;

  await apiInstance.sendTransacEmail(sendSmtpEmail);
  console.log(`[Email] Verification email sent to ${toEmail}`);
}
