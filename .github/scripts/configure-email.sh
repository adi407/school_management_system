#!/bin/bash
# Patches application-prod.yml on the server to enable Resend SMTP
CONFIG=/opt/sms/app/application-prod.yml

# Replace the disabled mail block with Resend SMTP
if ! grep -q "smtp.resend.com" "$CONFIG"; then
  python3 << 'PYEOF'
import re
config_path = "/opt/sms/app/application-prod.yml"
with open(config_path) as f:
    content = f.read()

old_pattern = re.compile(r'  # ── Disable Mail.*?enable: false', re.DOTALL)
new_block = """  # ── Resend SMTP
  mail:
    host: smtp.resend.com
    port: 465
    username: resend
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true"""

if old_pattern.search(content):
    content = old_pattern.sub(new_block, content)
    with open(config_path, 'w') as f:
        f.write(content)
    print("Mail config patched to Resend SMTP")
else:
    print("Mail config already patched or pattern not found")
PYEOF
fi

# Add app.email block if not present
if ! grep -q "^app:" "$CONFIG"; then
  printf '\napp:\n  email:\n    from: noreply@schoolmanager.live\n    admin-to: adityakumar20111@gmail.com\n    frontend-url: https://schoolmanager.live\n    enabled: true\n' >> "$CONFIG"
  echo "Added app.email config block"
fi
