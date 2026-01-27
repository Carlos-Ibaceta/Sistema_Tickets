CREATE DATABASE  IF NOT EXISTS `sistema_tickets_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `sistema_tickets_db`;
-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: sistema_tickets_db
-- ------------------------------------------------------
-- Server version	8.4.7

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `auditoria_tickets`
--

DROP TABLE IF EXISTS `auditoria_tickets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auditoria_tickets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `estado_anterior` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `estado_nuevo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_cambio` datetime(6) DEFAULT NULL,
  `usuario_accion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ticket_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8qlrtfeva5v8hba4d7u7x1bnb` (`ticket_id`),
  CONSTRAINT `FK8qlrtfeva5v8hba4d7u7x1bnb` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auditoria_tickets`
--

LOCK TABLES `auditoria_tickets` WRITE;
/*!40000 ALTER TABLE `auditoria_tickets` DISABLE KEYS */;
/*!40000 ALTER TABLE `auditoria_tickets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categorias`
--

DROP TABLE IF EXISTS `categorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categorias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre_categoria` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categorias`
--

LOCK TABLES `categorias` WRITE;
/*!40000 ALTER TABLE `categorias` DISABLE KEYS */;
INSERT INTO `categorias` VALUES (1,'Hardware'),(2,'Software'),(3,'Insumos'),(4,'Red'),(5,'CAS-Chile');
/*!40000 ALTER TABLE `categorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `departamentos`
--

DROP TABLE IF EXISTS `departamentos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departamentos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre_departamento` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departamentos`
--

LOCK TABLES `departamentos` WRITE;
/*!40000 ALTER TABLE `departamentos` DISABLE KEYS */;
INSERT INTO `departamentos` VALUES (1,'Administración Municipal'),(2,'Operaciones'),(3,'Secretaría Municipal'),(4,'SECPLAN'),(5,'Medio Ambiente, Aseo y Ornato'),(6,'Dirección de Obras Municipales (DOM)'),(7,'DIDECO'),(8,'Administración y Finanzas'),(9,'Control'),(10,'Jurídico'),(11,'Tránsito y Transporte Públicos'),(12,'Educación'),(13,'Salud (DESAM)'),(14,'Seguridad Pública'),(15,'Deportes, Turismo, Cultura y Recreación (DTCR)'),(16,'Juzgado de Policía Local'),(17,'Vivienda'),(18,'SENDA'),(19,'Farmacia Popular'),(20,'OLN'),(21,'Personal (RR.HH)'),(22,'Cementerio'),(23,'Prodesal'),(24,'Informatica');
/*!40000 ALTER TABLE `departamentos` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `estados_ticket`
--

DROP TABLE IF EXISTS `estados_ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `estados_ticket` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre_estado` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `estados_ticket`
--

LOCK TABLES `estados_ticket` WRITE;
/*!40000 ALTER TABLE `estados_ticket` DISABLE KEYS */;
INSERT INTO `estados_ticket` VALUES (1,'INGRESADO'),(2,'EN_PROCESO'),(3,'RESUELTO'),(5,'CANCELADO'),(6,'ESCALADO'),(7,'NO_RESUELTO');
/*!40000 ALTER TABLE `estados_ticket` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prioridades`
--

DROP TABLE IF EXISTS `prioridades`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prioridades` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nivel_prioridad` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prioridades`
--

LOCK TABLES `prioridades` WRITE;
/*!40000 ALTER TABLE `prioridades` DISABLE KEYS */;
INSERT INTO `prioridades` VALUES (1,'ALTA'),(2,'MEDIA'),(3,'BAJA');
/*!40000 ALTER TABLE `prioridades` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre_rol` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN'),(2,'SOPORTE'),(3,'FUNCIONARIO');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subcategorias`
--

DROP TABLE IF EXISTS `subcategorias`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subcategorias` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre_subcategoria` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `categoria_id` bigint NOT NULL,
  `prioridad_defecto_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKiucm5ipf0wvec50s8j67r33rk` (`categoria_id`),
  KEY `FK_sub_prio` (`prioridad_defecto_id`),
  CONSTRAINT `FK_sub_prio` FOREIGN KEY (`prioridad_defecto_id`) REFERENCES `prioridades` (`id`),
  CONSTRAINT `FKiucm5ipf0wvec50s8j67r33rk` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subcategorias`
--

LOCK TABLES `subcategorias` WRITE;
/*!40000 ALTER TABLE `subcategorias` DISABLE KEYS */;
INSERT INTO `subcategorias` VALUES (1,'Contabilidad',5,NULL),(2,'Tesorería',5,NULL),(3,'Adquisiciones',5,NULL),(4,'Remuneraciones',5,NULL),(5,'Personal',5,NULL),(6,'Licencia de conducir',5,NULL),(7,'Permiso de circulación',5,NULL),(8,'Patentes comerciales',5,NULL);
/*!40000 ALTER TABLE `subcategorias` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tickets`
--

DROP TABLE IF EXISTS `tickets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tickets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asunto` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` text COLLATE utf8mb4_unicode_ci,
  `fecha_creacion` datetime(6) NOT NULL,
  `departamento_id` bigint NOT NULL,
  `estado_ticket_id` bigint NOT NULL,
  `prioridad_id` bigint DEFAULT NULL,
  `subcategoria_id` bigint DEFAULT NULL,
  `usuario_id` bigint NOT NULL,
  `tecnico_asignado_id` bigint DEFAULT NULL,
  `categoria_id` bigint DEFAULT NULL,
  `fecha_incidente` datetime(6) DEFAULT NULL,
  `evidencia` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fecha_asignacion` datetime(6) DEFAULT NULL,
  `fecha_cierre` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK29nu82gfsgm04307hk62tavn4` (`departamento_id`),
  KEY `FK7y07nsy4iw4kyx8mtfme97og5` (`estado_ticket_id`),
  KEY `FKd7c02geg9xndboomrkkajs18u` (`prioridad_id`),
  KEY `FKkkg8jnbn2iv6mcfu3pr2byus5` (`subcategoria_id`),
  KEY `FKblaq7syvrsnelr3armtq2r9rm` (`usuario_id`),
  KEY `FK773qstcwn2vvjfhadool42m43` (`tecnico_asignado_id`),
  KEY `FK945st5u4inomber9a3k8loqq6` (`categoria_id`),
  CONSTRAINT `FK29nu82gfsgm04307hk62tavn4` FOREIGN KEY (`departamento_id`) REFERENCES `departamentos` (`id`),
  CONSTRAINT `FK773qstcwn2vvjfhadool42m43` FOREIGN KEY (`tecnico_asignado_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FK7y07nsy4iw4kyx8mtfme97og5` FOREIGN KEY (`estado_ticket_id`) REFERENCES `estados_ticket` (`id`),
  CONSTRAINT `FK945st5u4inomber9a3k8loqq6` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`),
  CONSTRAINT `FKblaq7syvrsnelr3armtq2r9rm` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKd7c02geg9xndboomrkkajs18u` FOREIGN KEY (`prioridad_id`) REFERENCES `prioridades` (`id`),
  CONSTRAINT `FKkkg8jnbn2iv6mcfu3pr2byus5` FOREIGN KEY (`subcategoria_id`) REFERENCES `subcategorias` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tickets`
--

LOCK TABLES `tickets` WRITE;
/*!40000 ALTER TABLE `tickets` DISABLE KEYS */;
/*!40000 ALTER TABLE `tickets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuarios` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `rut` varchar(12) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activo` bit(1) DEFAULT b'1',
  `nombre` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `rol_id` bigint NOT NULL,
  `kpis_habilitados` bit(1) DEFAULT NULL,
  `crear_usuarios_habilitado` bit(1) DEFAULT NULL,
  `departamento_id` bigint DEFAULT NULL,
  `reset_token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `token_expiration` datetime(6) DEFAULT NULL,
  `cambio_password_obligatorio` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKkfsp0s1tflm1cwlj8idhqsad0` (`email`),
  UNIQUE KEY `rut` (`rut`),
  KEY `FKqf5elo4jcq7qrt83oi0qmenjo` (`rol_id`),
  KEY `FKlayeuvn3gr7ql1o7532lxpfak` (`departamento_id`),
  CONSTRAINT `FKlayeuvn3gr7ql1o7532lxpfak` FOREIGN KEY (`departamento_id`) REFERENCES `departamentos` (`id`),
  CONSTRAINT `FKqf5elo4jcq7qrt83oi0qmenjo` FOREIGN KEY (`rol_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuarios`
--

LOCK TABLES `usuarios` WRITE;
/*!40000 ALTER TABLE `usuarios` DISABLE KEYS */;
INSERT INTO `usuarios` VALUES (1,'1-91','robottickets@gmail.com',_binary '','Carlos Ibaceta','$2a$10$6fVMSxvPSKmHutbRzqku8OskVxrNNB.DGeiWbgkEL4TpF8ZxGyjvy',1,_binary '\0',_binary '\0',24,'8376bb56-eea8-4b25-8cc6-df57fb17de71','2026-01-23 16:32:03.702018',_binary '\0');
/*!40000 ALTER TABLE `usuarios` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-27  9:17:16
