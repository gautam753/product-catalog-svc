CREATE TABLE public.user_account (
	user_id uuid NOT NULL DEFAULT gen_random_uuid(),
	"name" varchar(255) NULL,
	email public."citext" NULL,
	phone varchar(20) NULL,
	email_verified bool NULL DEFAULT false,
	phone_verified bool NULL DEFAULT false,
	hashed_password varchar(255) NULL,
	status varchar(20) NULL,
	account_type varchar(20) NULL DEFAULT 'customer'::character varying,
	mfa_setting varchar(20) NULL DEFAULT 'inactive'::character varying,
	mfa_methods varchar(255) NULL,
	created_at timestamp NULL DEFAULT now(),
	updated_at timestamp NULL DEFAULT now(),
	CONSTRAINT chk_email_or_phone CHECK (((email IS NOT NULL) OR (phone IS NOT NULL))),
	CONSTRAINT user_account_account_type_check CHECK (((account_type)::text = ANY ((ARRAY['customer'::character varying, 'guest'::character varying, 'admin'::character varying, 'seller'::character varying])::text[]))),
	CONSTRAINT user_account_email_key UNIQUE (email),
	CONSTRAINT user_account_phone_key UNIQUE (phone),
	CONSTRAINT user_account_pkey PRIMARY KEY (user_id)
);
CREATE INDEX idx_user_email ON public.user_account USING btree (email);
CREATE INDEX idx_user_phone ON public.user_account USING btree (phone);

CREATE TABLE public.identity_provider_account (
	identity_id uuid NOT NULL DEFAULT gen_random_uuid(),
	user_id uuid NOT NULL,
	provider varchar(50) NOT NULL,
	provider_user_id varchar(255) NOT NULL,
	metadata jsonb NULL,
	linked_at timestamp NULL DEFAULT now(),
	CONSTRAINT identity_provider_account_pkey PRIMARY KEY (identity_id),
	CONSTRAINT uq_identity_provider UNIQUE (provider, provider_user_id),
	CONSTRAINT fk_identity_user FOREIGN KEY (user_id) REFERENCES public.user_account(user_id) ON DELETE CASCADE
);

CREATE TABLE public.user_address (
	address_id uuid NOT NULL DEFAULT gen_random_uuid(),
	user_id uuid NOT NULL,
	"type" varchar(20) NULL,
	address_json jsonb NOT NULL,
	is_default bool NULL DEFAULT false,
	created_at timestamp NULL DEFAULT now(),
	updated_at timestamp NULL DEFAULT now(),
	CONSTRAINT user_address_pkey PRIMARY KEY (address_id),
	CONSTRAINT user_address_type_check CHECK (((type)::text = ANY ((ARRAY['home'::character varying, 'work'::character varying, 'shipping'::character varying, 'billing'::character varying])::text[]))),
	CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES public.user_account(user_id) ON DELETE CASCADE
);
CREATE INDEX idx_address_user ON public.user_address USING btree (user_id);

CREATE TABLE public.user_profile (
	user_id uuid NOT NULL,
	first_name varchar(100) NULL,
	last_name varchar(100) NULL,
	dob date NULL,
	gender varchar(20) NULL,
	preferences jsonb NULL,
	updated_at timestamp NULL DEFAULT now(),
	CONSTRAINT user_profile_pkey PRIMARY KEY (user_id),
	CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES public.user_account(user_id) ON DELETE CASCADE
);

CREATE TABLE public.carts (
	id int8 NOT NULL GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE),
	user_id varchar(255) NULL,
	guest_token varchar(255) NULL,
	owner_type varchar(10) NOT NULL,
	expires_at timestamptz NULL,
	created_at timestamptz NOT NULL DEFAULT now(),
	updated_at timestamptz NOT NULL DEFAULT now(),
	CONSTRAINT carts_owner_type_check CHECK (((owner_type)::text = ANY ((ARRAY['USER'::character varying, 'GUEST'::character varying])::text[]))),
	CONSTRAINT carts_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_carts_expires_at ON public.carts USING btree (expires_at);
CREATE INDEX idx_carts_guest_token ON public.carts USING btree (guest_token);
CREATE INDEX idx_carts_user_id ON public.carts USING btree (user_id);

CREATE TABLE public.cart_items (
	id int8 NOT NULL GENERATED ALWAYS AS IDENTITY( INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 1 CACHE 1 NO CYCLE),
	cart_id int8 NOT NULL,
	product_id varchar(255) NOT NULL,
	variant_id varchar(255) NOT NULL DEFAULT 'DEFAULT'::character varying,
	quantity int4 NOT NULL,
	price_at_addition numeric(10, 2) NOT NULL,
	added_at timestamptz NOT NULL DEFAULT now(),
	updated_at timestamptz NOT NULL DEFAULT now(),
	CONSTRAINT cart_items_pkey PRIMARY KEY (id),
	CONSTRAINT uk_cart_product_variant UNIQUE (cart_id, product_id, variant_id),
	CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES public.carts(id) ON DELETE CASCADE
);
CREATE INDEX idx_cart_items_cart_id ON public.cart_items USING btree (cart_id);
CREATE INDEX idx_cart_items_product_id ON public.cart_items USING btree (product_id);

CREATE TABLE wishlist_items (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     VARCHAR(255)    NOT NULL,
    product_id  VARCHAR(255)    NOT NULL,
    variant_id  VARCHAR(255)    NULL,       -- NULL means "no variant selected"
    priority    VARCHAR(10)     NULL CHECK (priority IN ('low', 'medium', 'high')),
    notes       TEXT            NULL,
    added_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_wishlist_with_variant
        UNIQUE (user_id, product_id, variant_id)

);
CREATE INDEX idx_wishlist_user_id ON wishlist_items(user_id);