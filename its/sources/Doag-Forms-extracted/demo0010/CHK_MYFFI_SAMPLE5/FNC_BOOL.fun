FUNCTION fnc_BOOL(p_bool BOOLEAN) RETURN VARCHAR2 IS
BEGIN
  IF p_bool THEN RETURN('TRUE');
  ELSE           RETURN('FALSE');
  END IF;
END;