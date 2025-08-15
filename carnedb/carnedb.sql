-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Versión del servidor:         9.4.0 - MySQL Community Server - GPL
-- SO del servidor:              Win64
-- HeidiSQL Versión:             12.11.0.7065
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- Volcando datos para la tabla carnedb.cajonpollo: ~0 rows (aproximadamente)
DELETE FROM `cajonpollo`;

-- Volcando datos para la tabla carnedb.cliente: ~0 rows (aproximadamente)
DELETE FROM `cliente`;

-- Volcando datos para la tabla carnedb.detallecajonpollo: ~0 rows (aproximadamente)
DELETE FROM `detallecajonpollo`;

-- Volcando datos para la tabla carnedb.detallemediares: ~0 rows (aproximadamente)
DELETE FROM `detallemediares`;
INSERT INTO `detallemediares` (`id`, `porcentajeCorte`, `media_id`, `producto_id`) VALUES
	(1, 3.2, 1, 67),
	(2, 5.55, 1, 68),
	(3, 1.62, 1, 69),
	(4, 0.18, 1, 70),
	(5, 1.11, 1, 72),
	(6, 4.22, 1, 73),
	(7, 2.23, 1, 74),
	(8, 2.49, 1, 77),
	(9, 2.65, 1, 78),
	(10, 1.43, 1, 79),
	(11, 3.83, 1, 80),
	(12, 4.38, 1, 81),
	(13, 1.48, 1, 82),
	(14, 5.51, 1, 83),
	(15, 2.13, 1, 84),
	(16, 3.87, 1, 85),
	(17, 2.2, 1, 86),
	(18, 1.44, 1, 87),
	(19, 4.69, 1, 88),
	(20, 0.41, 1, 89),
	(21, 2.83, 1, 90),
	(22, 1.52, 1, 91),
	(23, 2.83, 1, 92),
	(24, 5.98, 1, 93),
	(25, 8.13, 1, 118),
	(26, 3.87, 1, 120),
	(27, 3.38, 1, 121),
	(28, 9.44, 1, 122),
	(29, 3.19, 1, 123),
	(30, 2.83, 1, 124);

-- Volcando datos para la tabla carnedb.detallepedido: ~0 rows (aproximadamente)
DELETE FROM `detallepedido`;

-- Volcando datos para la tabla carnedb.detalleventa: ~0 rows (aproximadamente)
DELETE FROM `detalleventa`;

-- Volcando datos para la tabla carnedb.fiado: ~0 rows (aproximadamente)
DELETE FROM `fiado`;

-- Volcando datos para la tabla carnedb.fiadoparcial: ~0 rows (aproximadamente)
DELETE FROM `fiadoparcial`;

-- Volcando datos para la tabla carnedb.mediares: ~1 rows (aproximadamente)
DELETE FROM `mediares`;
INSERT INTO `mediares` (`id`, `fecha`, `pesoBoleta`, `pesoFinal`, `pesoPilon`, `precio`, `proveedor`) VALUES
	(1, '2025-08-15 14:14:25.423537', 90, 89.76, 88, 7000, 'belgrano');

-- Volcando datos para la tabla carnedb.pedido: ~0 rows (aproximadamente)
DELETE FROM `pedido`;

-- Volcando datos para la tabla carnedb.producto: ~65 rows (aproximadamente)
DELETE FROM `producto`;
INSERT INTO `producto` (`id`, `codigo`, `nombre`, `pesoParaVender`, `pesoPorUnidad`, `precio`, `tipo`, `stock_id`) VALUES
	(67, 6, 'Bola de lomo', NULL, 3.21, 13200, 'Carniceria', 1),
	(68, 7, 'Caracu', NULL, 5.56, 8180, 'Carniceria', 2),
	(69, 8, 'Cartilago', NULL, 1.62, 2500, 'Carniceria', 3),
	(70, 9, 'Chiquizuela', NULL, 0.18, 5200, 'Carniceria', 4),
	(71, 10, 'Chorizo', NULL, 0.135, 8900, 'Preparados', NULL),
	(72, 11, 'Colita de cuadril', NULL, 1.11, 14500, 'Carniceria', 5),
	(73, 12, 'Cuadrada', NULL, 4.23, 14500, 'Carniceria', 6),
	(74, 13, 'Cuadril', NULL, 2.24, 14500, 'Carniceria', 7),
	(75, 40, 'Hamburguesa', NULL, 0.8, 9000, 'Preparados', NULL),
	(76, 38, 'Medallon Jamon y q.', NULL, 0.3, 8500, 'Seco', NULL),
	(77, 14, 'Espinazo', NULL, 2.5, 6140, 'Carniceria', 8),
	(78, 15, 'Falda', NULL, 2.66, 9300, 'Carniceria', 9),
	(79, 16, 'Matambre', NULL, 1.43, 15070, 'Carniceria', 10),
	(80, 17, 'Nalga', NULL, 3.84, 14700, 'Carniceria', 11),
	(81, 18, 'Paleta', NULL, 4.39, 11530, 'Carniceria', 12),
	(82, 19, 'Peceto', NULL, 1.48, 15250, 'Carniceria', 13),
	(83, 20, 'Picada comun', NULL, 5.52, 10200, 'Carniceria', 14),
	(84, 21, 'Picada especial', NULL, 2.14, 11500, 'Carniceria', 15),
	(85, 22, 'Roast beef', NULL, 3.88, 10970, 'Carniceria', 16),
	(86, 23, 'Tapa de asado', NULL, 2.21, 12650, 'Carniceria', 17),
	(87, 24, 'Tapa de nalga', NULL, 1.44, 13950, 'Carniceria', 18),
	(88, 25, 'Vacio', NULL, 4.7, 12830, 'Carniceria', 19),
	(89, 26, 'Entraña', NULL, 0.41, 14130, 'Carniceria', 20),
	(90, 27, 'Americano', NULL, 2.84, 15540, 'Carniceria', 21),
	(91, 28, 'Lomo', NULL, 1.52, 15540, 'Carniceria', 22),
	(92, 29, 'Bife de chori', NULL, 2.84, 15540, 'Carniceria', 23),
	(93, 39, 'Grasa derretida', NULL, 6, 2500, 'Carniceria', 24),
	(94, 42, 'Corazon', NULL, 1, 1820, 'Achuras', NULL),
	(95, 37, 'Medallon Pollo y verdura', NULL, 0.8, 8500, 'Seco', NULL),
	(96, 38, 'Medallon Jamon y queso', NULL, 0.8, 8500, 'Seco', NULL),
	(97, 54, 'Mila de merluza', NULL, 0.25, 11000, 'Seco', NULL),
	(98, 56, 'Papas caritas', NULL, 0.1, 9350, 'Seco', NULL),
	(99, 57, 'Patitas rebozadas', NULL, 0.1, 7000, 'Seco', NULL),
	(100, 58, 'Papas baston', NULL, 0.1, 9350, 'Seco', NULL),
	(101, 65, 'Ricosaurios', NULL, 0.2, 8550, 'Seco', NULL),
	(102, 41, 'Chinchulin', NULL, 1, 6200, 'Achuras', NULL),
	(103, 43, 'Higado', NULL, 3, 4500, 'Achuras', NULL),
	(104, 44, 'Lengua', NULL, 2, 11300, 'Achuras', NULL),
	(105, 45, 'Mondongo', NULL, 3, 10800, 'Achuras', NULL),
	(106, 46, 'Rabo', NULL, 1.5, 9500, 'Achuras', NULL),
	(107, 47, 'Riñon', NULL, 0.3, 6500, 'Achuras', NULL),
	(108, 50, 'Molleja', NULL, 0.2, 21450, 'Achuras', NULL),
	(109, 51, 'Tripa gorda', NULL, 0.6, 3500, 'Achuras', NULL),
	(110, 68, 'Patita de cerdo', NULL, 0.2, 2000, 'Cerdo', NULL),
	(111, 70, 'Bife de cerdo', NULL, 4, 7200, 'Cerdo', NULL),
	(112, 71, 'Pechito de cerdo', NULL, 5, 7200, 'Cerdo', NULL),
	(113, 78, 'Bondiola', NULL, 2.5, 10000, 'Cerdo', NULL),
	(114, 49, 'Morcilla', NULL, 2, 5800, 'Preparados', NULL),
	(115, 61, 'Albondigas', NULL, 0.1, 9200, 'Preparados', NULL),
	(116, 64, 'Panceta', NULL, 1, 12000, 'Preparados', NULL),
	(117, 66, 'Matambre cocido', NULL, 0.1, 16500, 'Preparados', NULL),
	(118, 67, 'Grasa', NULL, 8.15, 1000, 'Carniceria', 25),
	(119, 59, 'Carbon chico', NULL, 5, 3500, 'Varios', NULL),
	(120, 1, 'Aguja comun', NULL, 3.88, 10600, 'Carniceria', 26),
	(121, 2, 'Aguja especial', NULL, 3.39, 11350, 'Carniceria', 27),
	(122, 3, 'Asado', NULL, 9.47, 12100, 'Carniceria', 28),
	(123, 4, 'Bife ancho', NULL, 3.2, 11530, 'Carniceria', 29),
	(124, 5, 'Bife angosto', NULL, 2.84, 11900, 'Carniceria', 30),
	(125, 30, 'Pollo entero', NULL, 2.8, 4520, 'promociones', NULL),
	(126, 31, 'Pata y muslo', NULL, 8.068, 3500, 'Pollo', NULL),
	(127, 32, 'Alitas', NULL, 2.926, 3000, 'Pollo', NULL),
	(128, 33, 'Supremas', NULL, 5.178, 9800, 'Pollo', NULL),
	(129, 34, 'Carcaza/Menudo', NULL, 1.861, 1000, 'Pollo', NULL),
	(130, 35, 'Oferta pata y muslo', NULL, 1, 3300, 'promociones', NULL),
	(131, 36, 'Pechuga', NULL, 0.8, 9000, 'promociones', NULL);

-- Volcando datos para la tabla carnedb.stock: ~0 rows (aproximadamente)
DELETE FROM `stock`;
INSERT INTO `stock` (`id`, `cantidad`, `cantidadMinima`, `fecha`, `producto_id`) VALUES
	(1, 3.2, 0, '2025-08-15 14:14:25.522128', 67),
	(2, 5.55, 0, '2025-08-15 14:14:25.560933', 68),
	(3, 1.62, 0, '2025-08-15 14:14:25.583057', 69),
	(4, 0.18, 0, '2025-08-15 14:14:25.600751', 70),
	(5, 1.11, 0, '2025-08-15 14:14:25.617247', 72),
	(6, 4.22, 0, '2025-08-15 14:14:25.633439', 73),
	(7, 2.23, 0, '2025-08-15 14:14:25.648990', 74),
	(8, 2.49, 0, '2025-08-15 14:14:25.664858', 77),
	(9, 2.65, 0, '2025-08-15 14:14:25.680544', 78),
	(10, 1.43, 0, '2025-08-15 14:14:25.698139', 79),
	(11, 3.83, 0, '2025-08-15 14:14:25.715705', 80),
	(12, 4.38, 0, '2025-08-15 14:14:25.730573', 81),
	(13, 1.48, 0, '2025-08-15 14:14:25.748372', 82),
	(14, 5.51, 0, '2025-08-15 14:14:25.764265', 83),
	(15, 2.13, 0, '2025-08-15 14:14:25.781390', 84),
	(16, 3.87, 0, '2025-08-15 14:14:25.796253', 85),
	(17, 2.2, 0, '2025-08-15 14:14:25.811675', 86),
	(18, 1.44, 0, '2025-08-15 14:14:25.826419', 87),
	(19, 4.69, 0, '2025-08-15 14:14:25.842648', 88),
	(20, 0.41, 0, '2025-08-15 14:14:25.858425', 89),
	(21, 2.83, 0, '2025-08-15 14:14:25.871387', 90),
	(22, 1.52, 0, '2025-08-15 14:14:25.902196', 91),
	(23, 2.83, 0, '2025-08-15 14:14:25.917676', 92),
	(24, 5.98, 0, '2025-08-15 14:14:25.933161', 93),
	(25, 8.13, 0, '2025-08-15 14:14:25.947442', 118),
	(26, 3.87, 0, '2025-08-15 14:14:25.962535', 120),
	(27, 3.38, 0, '2025-08-15 14:14:25.979125', 121),
	(28, 9.44, 0, '2025-08-15 14:14:25.994509', 122),
	(29, 3.19, 0, '2025-08-15 14:14:26.009836', 123),
	(30, 2.83, 0, '2025-08-15 14:14:26.023959', 124);

-- Volcando datos para la tabla carnedb.venta: ~0 rows (aproximadamente)
DELETE FROM `venta`;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
