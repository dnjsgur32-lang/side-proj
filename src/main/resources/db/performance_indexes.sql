-- KAMCO 입찰 데이터 성능 최적화 인덱스

-- 1. 검색 필터용 인덱스
CREATE INDEX idx_kamco_bids_sido ON kamco_bids(sido);
CREATE INDEX idx_kamco_bids_sgk ON kamco_bids(sgk);
CREATE INDEX idx_kamco_bids_emd ON kamco_bids(emd);
CREATE INDEX idx_kamco_bids_dpsl_mtd_cd ON kamco_bids(dpsl_mtd_cd);

-- 2. 물건명 검색용 인덱스 (FULLTEXT)
CREATE FULLTEXT INDEX idx_kamco_bids_cltr_nm ON kamco_bids(cltr_nm);

-- 3. 복합 인덱스 (자주 함께 검색되는 컬럼들)
CREATE INDEX idx_kamco_bids_location ON kamco_bids(sido, sgk, emd);
CREATE INDEX idx_kamco_bids_price_range ON kamco_bids(min_bid_prc, apsl_ases_avg_amt);

-- 4. 정렬용 인덱스
CREATE INDEX idx_kamco_bids_created_at ON kamco_bids(created_at DESC);
CREATE INDEX idx_kamco_bids_updated_at ON kamco_bids(updated_at DESC);

-- 5. 사용자 입찰 조회용 인덱스
CREATE INDEX idx_bids_user_id ON bids(user_id);
CREATE INDEX idx_bids_user_date ON bids(user_id, bid_date DESC);

-- 6. 사용자 테이블 인덱스
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);