insert into user_model(id,email,password,role)
values(nextval('my_seq'),'admin@uns.ac.rs', 'password' ,'ADMIN'),
	   (nextval('my_seq'), 'user@uns.ac.rs', 'password', 'USER'),
	   (nextval('my_seq'), 'ognjenuser@uns.ac.rs', 'password', 'USER'),
	   (nextval('my_seq'), 'ognjenadmin@uns.ac.rs', 'password', 'ADMIN'),
	   (nextval('my_seq'), 'owner@uns.ac.rs', 'password', 'OWNER');