ALTER TABLE eip             ADD COLUMN defunct BOOLEAN DEFAULT false;
ALTER TABLE images          ADD COLUMN defunct BOOLEAN DEFAULT false;
ALTER TABLE instances       ADD COLUMN defunct BOOLEAN DEFAULT false;
ALTER TABLE security_groups ADD COLUMN defunct BOOLEAN DEFAULT false;
ALTER TABLE vpc             ADD COLUMN defunct BOOLEAN DEFAULT false;
