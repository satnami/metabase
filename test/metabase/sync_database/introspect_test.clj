(ns metabase.sync-database.introspect-test
  (:require [expectations :refer :all]
            [metabase.db :as db]
            [metabase.models.database :as database]
            [metabase.models.hydrate :as hydrate]
            [metabase.models.raw-column :refer [RawColumn], :as raw-column]
            [metabase.models.raw-table :refer [RawTable], :as raw-table]
            [metabase.test.util :as tu]))

(tu/resolve-private-fns metabase.sync-database.introspect
  save-all-table-columns! save-all-table-fks! create-raw-table! update-raw-table! disable-raw-tables!)

(defn get-tables [database-id]
  (->> (hydrate/hydrate (db/sel :many RawTable :database_id database-id) :columns)
       (mapv tu/boolean-ids-and-timestamps)))

;; save-all-table-fks
;; test case of multi schema with repeating table names
(expect
  [[{:id                  true,
     :raw_table_id        true,
     :name                "id",
     :active              true,
     :base_type           :IntegerField
     :is_pk               false,
     :fk_target_column_id false,
     :details             {},
     :created_at          true,
     :updated_at          true}
    {:id                  true,
     :raw_table_id        true,
     :name                "user_id",
     :active              true,
     :base_type           :IntegerField
     :is_pk               false,
     :fk_target_column_id false,
     :details             {},
     :created_at          true,
     :updated_at          true}]
   [{:id                  true,
     :raw_table_id        true,
     :name                "id",
     :active              true,
     :base_type           :IntegerField
     :is_pk               false,
     :fk_target_column_id false,
     :details             {},
     :created_at          true,
     :updated_at          true}
    {:id                  true,
     :raw_table_id        true,
     :name                "user_id",
     :active              true,
     :base_type           :IntegerField
     :is_pk               false,
     :fk_target_column_id true,
     :details             {},
     :created_at          true,
     :updated_at          true}]
   [{:id                  true,
     :raw_table_id        true,
     :name                "id",
     :active              true,
     :base_type           :IntegerField
     :is_pk               false,
     :fk_target_column_id false,
     :details             {},
     :created_at          true,
     :updated_at          true}
    {:id                  true,
     :raw_table_id        true,
     :name                "user_id",
     :active              true,
     :base_type           :IntegerField
     :is_pk               false,
     :fk_target_column_id false,
     :details             {},
     :created_at          true,
     :updated_at          true}]
   [{:id                  true,
     :raw_table_id        true,
     :name                "id",
     :active              true,
     :base_type           :IntegerField
     :is_pk               false,
     :fk_target_column_id false,
     :details             {},
     :created_at          true,
     :updated_at          true}
    {:id                  true,
     :raw_table_id        true,
     :name                "user_id",
     :active              true,
     :base_type           :IntegerField
     :is_pk               false,
     :fk_target_column_id true,
     :details             {},
     :created_at          true,
     :updated_at          true}]]
  (tu/with-temp* [database/Database  [{database-id :id}]
                  raw-table/RawTable  [{raw-table-id1 :id, :as table} {:database_id database-id, :schema "customer1", :name "photos"}]
                  raw-column/RawColumn [_ {:raw_table_id raw-table-id1, :name "id"}]
                  raw-column/RawColumn [_ {:raw_table_id raw-table-id1, :name "user_id"}]
                  raw-table/RawTable  [{raw-table-id2 :id, :as table1} {:database_id database-id, :schema "customer2", :name "photos"}]
                  raw-column/RawColumn [_ {:raw_table_id raw-table-id2, :name "id"}]
                  raw-column/RawColumn [_ {:raw_table_id raw-table-id2, :name "user_id"}]
                  raw-table/RawTable  [{raw-table-id3 :id, :as table2} {:database_id database-id, :schema nil, :name "users"}]
                  raw-column/RawColumn [_ {:raw_table_id raw-table-id3, :name "id"}]]
    (let [get-columns #(->> (db/sel :many RawColumn :raw_table_id raw-table-id1)
                            (mapv tu/boolean-ids-and-timestamps))]
      ;; original list should not have any fks
      [(get-columns)
       ;; now add a fk
       (do
         (save-all-table-fks! table [{:fk-column-name   "user_id"
                                      :dest-table       {:schema nil, :name "users"}
                                      :dest-column-name "id"}])
         (get-columns))
       ;; now remove the fk
       (do
         (save-all-table-fks! table [])
         (get-columns))
       ;; now add back a different fk
       (do
         (save-all-table-fks! table [{:fk-column-name   "user_id"
                                      :dest-table       {:schema "customer1", :name "photos"}
                                      :dest-column-name "id"}])
         (get-columns))])))

;; save-all-table-columns
(expect
  [[]
   [{:id           true,
     :raw_table_id true,
     :active       true,
     :name         "beak_size",
     :base_type    :IntegerField
     :is_pk        true
     :fk_target_column_id false
     :details      {:inches 7, :special-type "category"},
     :created_at   true,
     :updated_at   true}]
   [{:id           true,
     :raw_table_id true,
     :active       true,
     :name         "beak_size",
     :base_type    :IntegerField
     :is_pk        false
     :fk_target_column_id false
     :details      {:inches 8},
     :created_at   true,
     :updated_at   true}
    {:id           true,
     :raw_table_id true,
     :active       true,
     :name         "num_feathers",
     :base_type    :IntegerField
     :is_pk        false
     :fk_target_column_id false
     :details      {:count 10000},
     :created_at   true,
     :updated_at   true}]
   [{:id           true,
     :raw_table_id true,
     :active       false,
     :name         "beak_size",
     :base_type    :IntegerField
     :is_pk        false
     :fk_target_column_id false
     :details      {:inches 8},
     :created_at   true,
     :updated_at   true}
    {:id           true,
     :raw_table_id true,
     :active       true,
     :name         "num_feathers",
     :base_type    :IntegerField
     :is_pk        false
     :fk_target_column_id false
     :details      {:count 12000},
     :created_at   true,
     :updated_at   true}]
   [{:id           true,
     :raw_table_id true,
     :active       true,
     :name         "beak_size",
     :base_type    :IntegerField
     :is_pk        false
     :fk_target_column_id false
     :details      {:inches 8},
     :created_at   true,
     :updated_at   true}
    {:id           true,
     :raw_table_id true,
     :active       true,
     :name         "num_feathers",
     :base_type    :IntegerField
     :is_pk        false
     :fk_target_column_id false
     :details      {:count 12000},
     :created_at   true,
     :updated_at   true}]]
  (tu/with-temp* [database/Database  [{database-id :id}]
                  raw-table/RawTable [{raw-table-id :id, :as table} {:database_id database-id}]]
    (let [get-columns #(->> (db/sel :many RawColumn :raw_table_id raw-table-id)
                            (mapv tu/boolean-ids-and-timestamps))]
      ;; original list should be empty
      [(get-columns)
       ;; now add a column
       (do
         (save-all-table-columns! table [{:name "beak_size", :base-type :IntegerField, :details {:inches 7}, :pk? true, :special-type "category"}])
         (get-columns))
       ;; now add another column and modify the first
       (do
         (save-all-table-columns! table [{:name "beak_size", :base-type :IntegerField, :details {:inches 8}}
                                         {:name "num_feathers", :base-type :IntegerField, :details {:count 10000}}])
         (get-columns))
       ;; now remove the first column
       (do
         (save-all-table-columns! table [{:name "num_feathers", :base-type :IntegerField, :details {:count 12000}}])
         (get-columns))
       ;; lastly, resurrect the first column (this ensures uniqueness by name)
       (do
         (save-all-table-columns! table [{:name "beak_size", :base-type :IntegerField, :details {:inches 8}}
                                         {:name "num_feathers", :base-type :IntegerField, :details {:count 12000}}])
         (get-columns))])))

;; create-raw-table
(expect
  [[]
   [{:id          true
     :database_id true
     :active      true
     :schema      nil
     :name        "users"
     :details     {:a "b"}
     :columns     []
     :created_at  true
     :updated_at  true}]
   [{:id          true
     :database_id true
     :active      true
     :schema      nil
     :name        "users"
     :details     {:a "b"}
     :columns     []
     :created_at  true
     :updated_at  true}
    {:id          true
     :database_id true
     :active      true
     :schema      "aviary"
     :name        "toucanery"
     :details     {:owner "Cam"}
     :columns     [{:id           true
                    :raw_table_id true
                    :active       true
                    :name         "beak_size"
                    :base_type    :IntegerField
                    :is_pk        true
                    :fk_target_column_id false
                    :details      {:inches 7}
                    :created_at   true
                    :updated_at   true}]
     :created_at  true
     :updated_at  true}]]
  (tu/with-temp* [database/Database [{database-id :id, :as db}]]
    [(get-tables database-id)
     ;; now add a table
     (do
       (create-raw-table! database-id {:schema nil,
                                       :name "users",
                                       :details {:a "b"}
                                       :columns []})
       (get-tables database-id))
     ;; now add another table, this time with a couple columns and some fks
     (do
       (create-raw-table! database-id {:schema "aviary",
                                       :name "toucanery",
                                       :details {:owner "Cam"}
                                       :columns [{:name      "beak_size",
                                                  :base-type :IntegerField,
                                                  :pk?       true
                                                  :details   {:inches 7}}]})
       (get-tables database-id))]))


;; update-raw-table
(expect
  [[{:id          true
     :database_id true
     :active      true
     :schema      "aviary"
     :name        "toucanery"
     :details     {:owner "Cam"}
     :columns     []
     :created_at  true
     :updated_at  true}]
   [{:id          true
     :database_id true
     :active      true
     :schema      "aviary"
     :name        "toucanery"
     :details     {:owner "Cam", :sqft 10000}
     :columns     [{:id           true
                    :raw_table_id true
                    :active       true
                    :name         "beak_size"
                    :base_type    :IntegerField
                    :is_pk        true
                    :fk_target_column_id false
                    :details      {:inches 7}
                    :created_at   true
                    :updated_at   true}]
     :created_at  true
     :updated_at  true}]]
  (tu/with-temp* [database/Database  [{database-id :id, :as db}]
                  raw-table/RawTable [table {:database_id database-id
                                             :schema      "aviary",
                                             :name        "toucanery",
                                             :details     {:owner "Cam"}}]]
    [(get-tables database-id)
     ;; now update the table
     (do
       (update-raw-table! table {:schema  "aviary",
                                 :name    "toucanery",
                                 :details {:owner "Cam", :sqft 10000}
                                 :columns [{:name      "beak_size",
                                            :base-type :IntegerField,
                                            :pk?       true,
                                            :details   {:inches 7}}]})
       (get-tables database-id))]))


;; disable-raw-tables
(expect
  [[{:id          true
     :database_id true
     :active      true
     :schema      "a"
     :name        "1"
     :details     {}
     :columns     []
     :created_at  true
     :updated_at  true}
    {:id          true
     :database_id true
     :active      true
     :schema      "a"
     :name        "2"
     :details     {}
     :columns     [{:id           true
                    :raw_table_id true
                    :active       true
                    :name         "beak_size"
                    :base_type    :IntegerField
                    :is_pk        false
                    :fk_target_column_id false
                    :details      {}
                    :created_at   true
                    :updated_at   true}]
     :created_at  true
     :updated_at  true}]
   [{:id          true
     :database_id true
     :active      false
     :schema      "a"
     :name        "1"
     :details     {}
     :columns     []
     :created_at  true
     :updated_at  true}
    {:id          true
     :database_id true
     :active      false
     :schema      "a"
     :name        "2"
     :details     {}
     :columns     [{:id           true
                    :raw_table_id true
                    :active       false
                    :name         "beak_size"
                    :base_type    :IntegerField
                    :is_pk        false
                    :fk_target_column_id false
                    :details      {}
                    :created_at   true
                    :updated_at   true}]
     :created_at  true
     :updated_at  true}]]
  (tu/with-temp* [database/Database    [{database-id :id, :as db}]
                  raw-table/RawTable   [t1 {:database_id database-id, :schema "a", :name "1"}]
                  raw-table/RawTable   [t2 {:database_id database-id, :schema "a", :name "2"}]
                  raw-column/RawColumn [c1 {:raw_table_id (:id t2), :name "beak_size", :base_type :IntegerField}]]
    [(get-tables database-id)
     (do
       (disable-raw-tables! [(:id t1) (:id t2)])
       (get-tables database-id))]))


;; TODO: introspect-raw-table-and-update!
;; TODO: introspect-database-and-update-raw-tables!
;; make sure to test case where FK relationship tables are out of order