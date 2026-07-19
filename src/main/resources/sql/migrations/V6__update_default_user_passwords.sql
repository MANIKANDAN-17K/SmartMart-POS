-- V6: Standardize default user passwords
UPDATE users SET password_hash = '$2a$12$mBHKY8/cDztrAWpdB1UdneLcr2LvWkKFOnMo/Q4MiOwkQNUvVSB3m' WHERE username = 'admin';
UPDATE users SET password_hash = '$2a$12$2F1MQSy3Mi5oXiGPTCHPqOnt4PXYILXXQp3ZuZ.N2uUlO0kcXoueq' WHERE username = 'cashier';
