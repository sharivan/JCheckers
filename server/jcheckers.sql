DROP DATABASE IF EXISTS `jcheckers`;
CREATE DATABASE `jcheckers`;
USE `jcheckers`;

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `access_log`
--

DROP TABLE IF EXISTS `access_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `access_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(12) DEFAULT NULL,
  `when` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `compid` int(11) DEFAULT NULL,
  `ip` varchar(32) DEFAULT NULL,
  `game` varchar(32) DEFAULT NULL,
  `room` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=344 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `admin_changes`
--

DROP TABLE IF EXISTS `admin_changes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admin_changes` (
  `id` int(11) NOT NULL,
  `admin` varchar(12) DEFAULT NULL,
  `author` varchar(12) DEFAULT NULL,
  `when` timestamp NULL DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `admins`
--

DROP TABLE IF EXISTS `admins`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `admins` (
  `game` varchar(32) NOT NULL DEFAULT '',
  `room` int(11) NOT NULL DEFAULT '-1',
  `name` varchar(12) NOT NULL,
  `level` int(11) DEFAULT '0',
  `added_by` varchar(12) DEFAULT NULL,
  `added_when` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `active` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`game`,`room`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bans`
--

DROP TABLE IF EXISTS `bans`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `bans` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `game` varchar(32) DEFAULT NULL,
  `room` int(11) DEFAULT '-1',
  `user` varchar(12) DEFAULT NULL,
  `compid` int(11) DEFAULT NULL,
  `ip` varchar(32) DEFAULT NULL,
  `type` set('NICK','COMPID','IP') DEFAULT 'NICK',
  `restriction` enum('CHAT','ACCESS') DEFAULT 'ACCESS',
  `admin` varchar(12) DEFAULT NULL,
  `when` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `duration` bigint(20) DEFAULT '-1',
  `reason` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `boards_stats`
--

DROP TABLE IF EXISTS `boards_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `boards_stats` (
  `game` varchar(32) NOT NULL,
  `user` varchar(12) NOT NULL,
  `playeds` int(11) DEFAULT '0',
  `abandoneds` int(11) unsigned DEFAULT '0',
  `wins` int(11) unsigned DEFAULT '0',
  `losses` int(11) unsigned DEFAULT '0',
  `bomb` int(11) unsigned DEFAULT '0',
  `draws` int(11) unsigned DEFAULT '0',
  `rating` int(11) unsigned DEFAULT '1200',
  `streak` int(11) DEFAULT '0',
  PRIMARY KEY (`game`,`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `compids`
--

DROP TABLE IF EXISTS `compids`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `compids` (
  `compid` int(11) NOT NULL,
  `times_used` int(11) DEFAULT '0',
  `last_time_used_by` varchar(12) DEFAULT NULL,
  `last_time_used_when` timestamp NULL DEFAULT NULL,
  `banned` bit(1) DEFAULT b'0',
  `banned_when` timestamp NULL DEFAULT NULL,
  `ban_duration` int(11) DEFAULT NULL,
  `ban_reason` text,
  PRIMARY KEY (`compid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `emails`
--

DROP TABLE IF EXISTS `emails`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `emails` (
  `email` varchar(64) NOT NULL,
  `times_using` int(11) DEFAULT '0',
  `times_used` int(11) DEFAULT '0',
  `last_time_used_by` varchar(12) DEFAULT NULL,
  `last_time_used_when` timestamp NULL DEFAULT NULL,
  `banned` bit(1) DEFAULT b'0',
  `banned_when` timestamp NULL DEFAULT NULL,
  `ban_duration` int(11) unsigned DEFAULT NULL,
  `ban_reason` text,
  PRIMARY KEY (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `friends`
--

DROP TABLE IF EXISTS `friends`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `friends` (
  `user` varchar(12) NOT NULL,
  `friend` varchar(12) DEFAULT NULL,
  `added_when` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `game_log`
--

DROP TABLE IF EXISTS `game_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `game_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `game` varchar(32) DEFAULT NULL,
  `room` int(11) DEFAULT NULL,
  `rated` bit(1) DEFAULT b'1',
  `player1` varchar(12) DEFAULT NULL,
  `old_rating1` int(11) DEFAULT NULL,
  `new_rating1` int(11) DEFAULT NULL,
  `player2` varchar(12) DEFAULT NULL,
  `old_rating2` int(11) DEFAULT NULL,
  `new_rating2` int(11) DEFAULT NULL,
  `played_when` timestamp NULL DEFAULT NULL,
  `result` int(11) DEFAULT NULL,
  `winner` int(11) DEFAULT NULL,
  `data` blob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `games`
--

DROP TABLE IF EXISTS `games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `games` (
  `name` varchar(32) NOT NULL,
  `title` text,
  `description` text,
  `image` text,
  `host` text,
  `port` int(11) DEFAULT NULL,
  `data_host` text,
  `http_host` text,
  `min_rating` int(11) DEFAULT '0',
  `max_rating` int(11) DEFAULT '10000',
  `initial_rating` int(11) DEFAULT '1000',
  `users` int(11) DEFAULT '0',
  `plugin_name` text,
  `plugin_major_version` int(11) DEFAULT NULL,
  `plugin_minor_version` int(11) DEFAULT NULL,
  `plugin_game_name` text,
  `path` text,
  `big_path` text,
  `help_path` text,
  `active` bit(1) DEFAULT b'1',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `games`
--

LOCK TABLES `games` WRITE;
/*!40000 ALTER TABLE `games` DISABLE KEYS */;
INSERT INTO `games` VALUES ('draughts','Damas','Damas',NULL,'localhost',5085,'localhost','localhost',0,10000,1200,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,_binary '');
/*!40000 ALTER TABLE `games` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ignoreds`
--

DROP TABLE IF EXISTS `ignoreds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ignoreds` (
  `user` varchar(12) NOT NULL,
  `ignored` varchar(32) NOT NULL,
  `ignored_when` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`user`,`ignored`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ips`
--

DROP TABLE IF EXISTS `ips`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ips` (
  `ip` varchar(32) NOT NULL,
  `times_used` int(11) DEFAULT '0',
  `last_time_used_by` varchar(12) DEFAULT NULL,
  `last_time_used_when` timestamp NULL DEFAULT NULL,
  `banned` bit(1) DEFAULT b'0',
  `banned_when` timestamp NULL DEFAULT NULL,
  `ban_duration` int(11) DEFAULT NULL,
  `ban_reason` text,
  PRIMARY KEY (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `login_log`
--

DROP TABLE IF EXISTS `login_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `login_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(12) DEFAULT NULL,
  `when` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ip` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=155 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,NO_AUTO_VALUE_ON_ZERO,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `jcheckers`.`login_log_AFTER_INSERT` AFTER INSERT ON `login_log` FOR EACH ROW
BEGIN
	UPDATE users SET last_login = NEW.id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `passwords`
--

DROP TABLE IF EXISTS `passwords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `passwords` (
  `password` varchar(32) NOT NULL,
  `times_used` int(11) DEFAULT '0',
  `last_time_used_by` varchar(12) DEFAULT NULL,
  `last_time_used_wuen` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`password`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `rooms`
--

DROP TABLE IF EXISTS `rooms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rooms` (
  `game` varchar(32) NOT NULL,
  `number` int(11) NOT NULL,
  `name` text,
  `max_players` int(11) NOT NULL DEFAULT '40',
  `with_admin` bit(1) NOT NULL DEFAULT b'1',
  `public` bit(1) NOT NULL DEFAULT b'1',
  `users` int(11) DEFAULT '0',
  `active` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`game`,`number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rooms`
--

LOCK TABLES `rooms` WRITE;
/*!40000 ALTER TABLE `rooms` DISABLE KEYS */;
INSERT INTO `rooms` VALUES ('draughts',1,'Iniciantes',40,_binary '',_binary '',0,_binary ''),('draughts',2,'Social 1',40,_binary '',_binary '',0,_binary ''),('draughts',3,'Social 2',40,_binary '',_binary '',0,_binary ''),('draughts',4,'Social 3',40,_binary '',_binary '',0,_binary '');
/*!40000 ALTER TABLE `rooms` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sessions`
--

DROP TABLE IF EXISTS `sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sessions` (
  `game` varchar(32) NOT NULL,
  `login` varchar(12) NOT NULL,
  `session_id` varchar(32) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `renewed` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `expires` int(11) NOT NULL DEFAULT '1440',
  PRIMARY KEY (`game`,`login`,`session_id`),
  KEY `sessions.games_idx` (`game`),
  KEY `sessions.users_ids` (`login`),
  CONSTRAINT `sessions.games` FOREIGN KEY (`game`) REFERENCES `games` (`name`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `sessions.users` FOREIGN KEY (`login`) REFERENCES `users` (`name`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `name` varchar(12) CHARACTER SET utf8 NOT NULL,
  `password` varchar(32) CHARACTER SET utf8 DEFAULT NULL,
  `registered` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `email` varchar(64) CHARACTER SET utf8 DEFAULT NULL,
  `verified` bit(1) NOT NULL DEFAULT b'0',
  `chips` bigint(20) NOT NULL DEFAULT '1000',
  `ingame_chips` bigint(20) NOT NULL DEFAULT '0',
  `last_login` int(11) DEFAULT NULL,
  `last_access` int(11) DEFAULT NULL,
  `banned` bit(1) NOT NULL DEFAULT b'0',
  `banned_when` timestamp NULL DEFAULT NULL,
  `ban_duration` int(10) unsigned NOT NULL DEFAULT '0',
  `ban_reason` text CHARACTER SET utf8,
  `system` bit(1) NOT NULL DEFAULT b'0',
  `active` bit(1) NOT NULL DEFAULT b'1',
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES ('root','root',NULL,'root@localhost',b'1',1000,0,NULL,NULL,b'0',NULL,0,NULL,b'1',b'1');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `views`
--

DROP TABLE IF EXISTS `views`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `views` (
  `viewed` varchar(12) NOT NULL DEFAULT '',
  `viewer` varchar(12) NOT NULL DEFAULT '',
  `count` int(11) NOT NULL DEFAULT '0',
  `last_viewer_compid` int(11) DEFAULT NULL,
  `last_viewer_ip` varchar(32) DEFAULT NULL,
  `last_visit_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`viewed`,`viewer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `vips`
--

DROP TABLE IF EXISTS `vips`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vips` (
  `user` varchar(12) NOT NULL,
  `type` int(11) DEFAULT NULL,
  `started_when` timestamp NULL DEFAULT NULL,
  `duration` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Procedures and functions
--

DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `IsLetter`(c CHAR) RETURNS tinyint(1)
BEGIN
	RETURN c >= 'A' AND c <= 'Z' OR c >= 'a' AND c <= 'z';
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `IsNumber`(c CHAR) RETURNS tinyint(1)
BEGIN
	RETURN c >= '0' AND c <= '9';
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `IsSeparator`(c CHAR) RETURNS tinyint(1)
BEGIN
	RETURN c = '.' OR c = '-' OR c = '_';
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `IsValidComponent`(component TEXT) RETURNS tinyint(1)
BEGIN
	DECLARE i INT;
    DECLARE len INT;
    DECLARE c CHAR;
    
    SET len = CHAR_LENGTH(component);
    SET i = 1;
    WHILE i <= len DO
		SET c = SUBSTRING(component, i, 1);
        IF NOT IsLetter(c) THEN
			RETURN FALSE;
		END IF;
        
		SET i = i + 1;
    END WHILE;

	RETURN TRUE;
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `IsValidEmail`(email TEXT) RETURNS tinyint(1)
BEGIN
	DECLARE p INT;
    DECLARE name TEXT;
    DECLARE domain TEXT;
    DECLARE component TEXT;
    DECLARE i INT;
    
    SET p = POSITION('@' IN email);
    IF p = 0 THEN
		RETURN FALSE;
    END IF;
    
    SET name = SUBSTRING(email, 1, p - 1);
    SET domain = SUBSTRING(email, p + 1, CHAR_LENGTH(email) - p);
    
    IF NOT IsValidName(name) THEN
		RETURN FALSE;
	END IF;
    
    SET p = POSITION('.' IN domain);
    IF P = 0 THEN
		RETURN FALSE;
	END IF;
    
    SET component = SUBSTRING(domain, 1, p - 1);
    SET domain = SUBSTRING(domain, p + 1, CHAR_LENGTH(domain) - p);
    IF NOT IsValidName(component) THEN
		RETURN FALSE;
	END IF;
    
    myloop: WHILE TRUE DO
		SET p = POSITION('.' IN domain);
		IF P = 0 THEN
			IF NOT IsValidComponent(domain) THEN
				RETURN FALSE;
			END IF;
            
			LEAVE myloop;
		ELSE
			SET component = SUBSTRING(domain, 1, p - 1);
			SET domain = SUBSTRING(domain, p + 1, CHAR_LENGTH(domain) - p);
			IF NOT IsValidComponent(component) THEN
				RETURN FALSE;
			END IF;
		END IF;
	END WHILE;
    
	RETURN TRUE;
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` FUNCTION `IsValidName`(name TEXT) RETURNS tinyint(1)
BEGIN
	DECLARE i INT;
    DECLARE len INT;
    DECLARE c CHAR;
    
    SET len = CHAR_LENGTH(name);
    SET i = 1;
    WHILE i <= len DO
		SET c = SUBSTRING(name, i, 1);
        IF NOT IsLetter(c) AND NOT IsNumber(c) AND NOT IsSeparator(c) THEN
			RETURN FALSE;
		END IF;
        
		SET i = i + 1;
    END WHILE;

	RETURN TRUE;
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `Login`(IN game TEXT, IN username TEXT, IN password TEXT, IN ip TEXT, OUT result INT, OUT sid TEXT)
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
    DECLARE msg TEXT;
	DECLARE pwd TEXT;
    DECLARE millis BIGINT(20);

	DECLARE cur CURSOR FOR SELECT users.password FROM users WHERE users.name = username;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
		GET DIAGNOSTICS CONDITION 1 code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
		ROLLBACK;
        SET sid = msg;
        SET result = -1;
    END;
    
    DECLARE EXIT HANDLER FOR NOT FOUND
    BEGIN
		ROLLBACK;
        SET result = 1;
    END;
    
    SET millis = ROUND(UNIX_TIMESTAMP(CURTIME(4)) * 1000);

	START TRANSACTION;
    
	OPEN cur;
	FETCH cur INTO pwd;
	CLOSE cur;
	
	IF password = pwd THEN
		SET result = 0;
		SET sid = MD5(CONCAT(username, MD5(millis ^ ROUND(32767 + RAND() * 3 * 32767))));
        INSERT INTO login_log VALUES(NULL, username, NULL, ip);
        INSERT INTO sessions VALUES(game, username, sid, NULL, NULL, 1440);
	ELSE
		SET result = 1;
	END IF;
        
	IF result = 0 THEN
		COMMIT;
	ELSE
		ROLLBACK;
	END IF;
END$$
DELIMITER ;

DELIMITER $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `Register`(IN username TEXT, IN password TEXT, IN email TEXT, OUT result INT, OUT message TEXT)
this_proc:
BEGIN
	DECLARE code CHAR(5) DEFAULT '00000';
    DECLARE msg TEXT;
	DECLARE chr CHAR;
    DECLARE has_separator BOOL;
    DECLARE len INT;
    DECLARE i INT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
		GET DIAGNOSTICS CONDITION 1 code = RETURNED_SQLSTATE, msg = MESSAGE_TEXT;
        SET message = msg;
        SET result = -1;
    END;
    
    DECLARE EXIT HANDLER FOR SQLSTATE '23000' SET result = 4;

	SET len = CHAR_LENGTH(username);
    
	IF len < 4 OR len > 12 THEN
		SET result = 1;
        LEAVE this_proc;
    END IF;
    
    SET chr = SUBSTRING(username, 1, 1);
    IF NOT IsLetter(chr) THEN
		SET result = 1;
        LEAVE this_proc;
    END IF;
    
    SET chr = SUBSTRING(username, len, 1);
    IF NOT IsLetter(chr) AND NOT IsNumber(chr) THEN
		SET result = 1;
        LEAVE this_proc;
    END IF;
    
    SET has_separator = FALSE;
    SET i = 1;
    WHILE i <= len DO
		SET chr = SUBSTRING(username, i, 1);
        IF IsSeparator(chr) THEN
			IF has_separator THEN
				SET result = 1;
				LEAVE this_proc;
            END IF;
            
            SET has_separator = TRUE;
		END IF;
        
		SET i = i + 1;
    END WHILE;
    
    SET len = CHAR_LENGTH(password);
    
	IF len < 4 OR len > 10 OR STRCMP(password COLLATE utf8_general_ci, username COLLATE utf8_general_ci) = 0 THEN
		SET result = 2;
        LEAVE this_proc;
    END IF;
    
    SET i = 1;
    WHILE i <= len DO
		SET chr = SUBSTRING(password, i, 1);
        IF chr < ' ' OR chr > '~' THEN
			SET result = 2;
			LEAVE this_proc;
		END IF;
        
		SET i = i + 1;
    END WHILE;
    
	IF NOT IsValidEmail(email) THEN
		SET result = 3;
		LEAVE this_proc;
	END IF;
    
    INSERT INTO users VALUES(username, password, NULL, email, FALSE, 0, 0, NULL, NULL, FALSE, NULL, 0, NULL, FALSE, TRUE);
    SET result = 0;
END$$
DELIMITER ;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;