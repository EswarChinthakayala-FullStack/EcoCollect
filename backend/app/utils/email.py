import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from app.config import settings

def send_otp_email(to_email: str, otp_code: str) -> bool:
    """
    Sends an OTP verification email to the user.
    """
    try:
        # Create message container
        msg = MIMEMultipart()
        msg['From'] = settings.SMTP_FROM
        msg['To'] = to_email
        msg['Subject'] = "EcoCollect - Password Reset Verification Code"
        
        # HTML template for a premium, beautiful email
        body = f"""
        <html>
        <body style="font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; background-color: #f8fafc; padding: 30px; margin: 0;">
          <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; border: 1px solid #e2e8f0; padding: 40px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);">
            <div style="text-align: center; margin-bottom: 30px;">
              <span style="font-size: 24px; font-weight: 800; background: linear-gradient(to right, #059669, #2563eb); -webkit-background-clip: text; color: #059669; letter-spacing: -0.02em;">EcoCollect</span>
            </div>
            <h2 style="font-size: 20px; font-weight: 700; color: #0f172a; margin-top: 0; margin-bottom: 16px; text-align: center;">Reset Your Password</h2>
            <p style="font-size: 15px; line-height: 1.6; color: #475569; margin-bottom: 24px; text-align: center;">
              You requested to reset your password. Use the following 6-digit verification code to complete the request:
            </p>
            <div style="text-align: center; margin-bottom: 30px;">
              <span style="display: inline-block; font-size: 32px; font-weight: 800; letter-spacing: 6px; color: #059669; padding: 12px 30px; background-color: #ecfdf5; border-radius: 12px; border: 1px solid #d1fae5;">
                {otp_code}
              </span>
            </div>
            <p style="font-size: 13px; line-height: 1.6; color: #94a3b8; text-align: center; margin-top: 30px; border-top: 1px solid #f1f5f9; padding-top: 20px;">
              This code will expire in 10 minutes. If you did not request this password reset, please ignore this email.
            </p>
          </div>
        </body>
        </html>
        """
        
        msg.attach(MIMEText(body, 'html'))
        
        # Connect to SMTP server
        server = smtplib.SMTP(settings.SMTP_HOST, settings.SMTP_PORT)
        server.starttls()  # Upgrade connection to secure encrypted SSL/TLS
        server.login(settings.SMTP_USER, settings.SMTP_PASSWORD)
        
        # Send email
        server.sendmail(settings.SMTP_FROM, to_email, msg.as_string())
        server.quit()
        print(f"[DEBUG] Verification OTP email sent successfully to {to_email}")
        return True
    except Exception as e:
        print(f"[ERROR] Failed to send OTP email to {to_email}: {e}")
        return False
