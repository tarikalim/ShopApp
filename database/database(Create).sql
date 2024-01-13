CREATE TABLE Products (
    ProductID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL ,
    Price DECIMAL(10, 2) NOT NULL,
    StockQuantity INT DEFAULT 0,
    Description TEXT,
    CategoryID INT,
    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID)
);


CREATE TABLE Users (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(50) NOT NULL UNIQUE,
    Password VARCHAR(50) NOT NULL,
    Email VARCHAR(100),
    Address TEXT,
    CreditCardID VARCHAR(16)
);

CREATE TABLE Cart (
    CartID INT AUTO_INCREMENT PRIMARY KEY,
    UserID INT,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

CREATE TABLE CartDetails (
    CartID INT,
    ProductID INT,
    Quantity INT DEFAULT 1,
    FOREIGN KEY (CartID) REFERENCES Cart(CartID),
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);
CREATE TABLE Orders (
    OrderID int AUTO_INCREMENT PRIMARY KEY,
    UserID int NOT NULL,
    OrderDate date NOT NULL,
    Status enum('Order Placed', 'Shipped', 'Delivered') NOT NULL,
    Address varchar(255) NOT NULL,
    PaymentType enum('Pay at the door', 'Credit Card') NOT NULL,
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);


CREATE TABLE OrderDetails (
    OrderID INT,
    ProductID INT,
    Quantity INT,
    SalePrice DECIMAL(10, 2),
    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID),
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);

CREATE TABLE Categories (
    CategoryID INT AUTO_INCREMENT PRIMARY KEY,
    CategoryName VARCHAR(100),
    Description TEXT
);

CREATE TABLE Review (
    ReviewID int AUTO_INCREMENT PRIMARY KEY,
    UserID int NOT NULL,
    ProductID int NOT NULL,
    Date date NOT NULL,
    Comment text NOT NULL,
    Rate int NOT NULL,
    FOREIGN KEY (UserID) REFERENCES Users(UserID),
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);



