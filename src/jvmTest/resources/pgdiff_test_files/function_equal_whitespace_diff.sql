
SET search_path = public, pg_catalog;

CREATE OR REPLACE FUNCTION update_list_subscription_history() RETURNS trigger
    LANGUAGE plpgsql
    AS $$ BEGIN INSERT INTO list_subscription_history (id, areaid, prefixid, msn, j4f_customer_id, subscriptiondate, seqid, refid, price, stopdate) VALUES (OLD.id, OLD.areaid, OLD.prefixid, OLD.msn, OLD.j4f_customer_id, OLD.subscriptiondate, OLD.seqid, OLD.refid, OLD.price, CURRENT_TIMESTAMP); RETURN OLD; END; $$;

ALTER FUNCTION update_list_subscription_history() OWNER TO postgres;
