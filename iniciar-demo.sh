#!/usr/bin/env sh
cd "$(dirname "$0")" || exit 1
exec java -Duser.timezone=America/Bogota -jar ejecutable/tecnoturno-1.0.0.jar --spring.profiles.active=demo
