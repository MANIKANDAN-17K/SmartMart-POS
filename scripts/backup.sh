#!/bin/bash
DB_NAME="smartmart_pos"
DB_USER="root"
DB_PASS="yourpassword"
BACKUP_DIR="./backups"
mkdir -p "$BACKUP_DIR"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
FILE="$BACKUP_DIR/${DB_NAME}_backup_${TIMESTAMP}.sql"
mysqldump -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" > "$FILE"
echo "Backup created: $FILE"