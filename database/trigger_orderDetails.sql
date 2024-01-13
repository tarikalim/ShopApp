DELIMITER //
CREATE TRIGGER calculate_sale_price
BEFORE INSERT ON OrderDetails
FOR EACH ROW
BEGIN
    -- Ürün fiyatını ve miktarını al
    DECLARE product_price DECIMAL(10, 2);
    DECLARE quantity INT;
    
    SELECT Price INTO product_price
    FROM Products
    WHERE ProductID = NEW.ProductID;
    
    SET NEW.SalePrice = product_price * NEW.Quantity;
END;
//
DELIMITER ;


