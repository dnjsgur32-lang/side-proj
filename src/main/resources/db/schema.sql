-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS onbid CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE onbid;

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 입찰 테이블
CREATE TABLE IF NOT EXISTS bids (
    bid_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pblnc_no VARCHAR(50) NOT NULL,
    pblnc_nm VARCHAR(500),
    bid_amount BIGINT NOT NULL,
    bid_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_pblnc_no (pblnc_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- KAMCO 입찰 정보 테이블
CREATE TABLE IF NOT EXISTS kamco_bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pbct_no VARCHAR(50) NOT NULL UNIQUE,
    cltr_nm VARCHAR(500),
    sido VARCHAR(50),
    sgk VARCHAR(50),
    emd VARCHAR(50),
    min_bid_prc BIGINT,
    apsl_ases_avg_amt BIGINT,
    pbct_bgn_dt VARCHAR(20),
    pbct_end_dt VARCHAR(20),
    dpsl_mtd_cd VARCHAR(10),
    ctgr_hirk_id VARCHAR(20),
    ctgr_hirk_id_mid VARCHAR(20),
    goods_price_from BIGINT,
    goods_price_to BIGINT,
    open_price_from BIGINT,
    open_price_to BIGINT,
    cltr_mnmt_no VARCHAR(50),
    sale_type VARCHAR(20),
    detail_address VARCHAR(500),
    appraisal_value BIGINT,
    deposit_amount BIGINT,
    bid_method VARCHAR(50),
    area_size VARCHAR(100),
    building_structure VARCHAR(100),
    land_use VARCHAR(100),
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_pbct_no (pbct_no),
    INDEX idx_sido (sido),
    INDEX idx_sgk (sgk),
    INDEX idx_emd (emd),
    INDEX idx_dpsl_mtd_cd (dpsl_mtd_cd),
    INDEX idx_cltr_nm (cltr_nm(100)),
    INDEX idx_pbct_dates (pbct_bgn_dt, pbct_end_dt),
    INDEX idx_price_range (min_bid_prc, apsl_ases_avg_amt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
