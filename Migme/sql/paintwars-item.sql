-- Create the table
CREATE TABLE paintwarsitem (
	ID int(11),
	Name varchar(128),
	Description varchar(256),
	Currency varchar(6),
	Price double
);

-- Insert the item data
INSERT INTO paintwarsitem (ID, Name, Description, Currency, Price) VALUES (1, 'Paint Proof', 'Prevents people from painting you for 1 day.', 'USD', 0.25);
INSERT INTO paintwarsitem (ID, Name, Description, Currency, Price) VALUES (2, 'Dual Paint', 'Double points for painting for 1 day.', 'USD', 0.10);
INSERT INTO paintwarsitem (ID, Name, Description, Currency, Price) VALUES (3, 'Stealth Paint', 'Paint someone under stealth, Player''s name will not be announced via emote message (Lasts for 1 day).', 'USD', 0.35);
