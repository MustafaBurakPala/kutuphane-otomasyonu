IF OBJECT_ID('dbo.book','U') IS NOT NULL OR OBJECT_ID('dbo.library_member','U') IS NOT NULL OR OBJECT_ID('dbo.loan','U') IS NOT NULL
    THROW 50000, 'Kutuphane tablolar zaten var; mevcut veri uzerine yazilmadi.', 1;
CREATE TABLE dbo.book (
 id bigint IDENTITY(1,1) PRIMARY KEY,
 title nvarchar(200) NULL,
 author nvarchar(150) NULL,
 isbn nvarchar(40) NULL UNIQUE,
 copies int NOT NULL
);
CREATE TABLE dbo.library_member (
 id bigint IDENTITY(1,1) PRIMARY KEY,
 name nvarchar(150) NULL,
 email nvarchar(200) NULL UNIQUE,
 phone nvarchar(30) NULL
);
CREATE TABLE dbo.loan (
 id bigint IDENTITY(1,1) PRIMARY KEY,
 book_id bigint NOT NULL REFERENCES dbo.book(id),
 member_id bigint NOT NULL REFERENCES dbo.library_member(id),
 borrowed_at date NULL,
 due_at date NULL,
 returned_at date NULL
);
