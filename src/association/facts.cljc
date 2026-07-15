(ns association.facts
  "Industry rule/best-practices catalog for the National Association of
  Broadcasters (NAB, Wikidata Q1759624) -- a 22nd
  industry-association-level source (see cloud-itonami-assoc-6419-jpn-zenginkyo,
  -6512-jpn-sonpo, -6612-jpn-jsda, -6419-deu-bankenverband, -6612-usa-finra,
  -6512-usa-naic, -6920-jpn-jicpa, -6920-usa-aicpa, -6419-fra-fbf,
  -6511-jpn-seiho, -6910-jpn-nichibenren, -6810-jpn-recaj, -6411-jpn-boj,
  -6120-usa-ctia, -5110-usa-a4a, -3510-usa-eei, -2910-deu-vda,
  -5510-usa-ahla, -2100-usa-phrma, -4719-usa-nrf, -4100-usa-agc for the
  first twenty-one) per ADR-2607141700 (cloud-itonami-compliance-fact-federation).
  The FIRST entry aligned to ISIC 6020 (television programming and
  broadcasting activities) -- a new industry code for this family. A
  rule not in this table has NO spec-basis, full stop; extend
  `catalog`, do not invent an id/url.

  The Political Broadcast Catechism (16th Edition) PDF was verified by
  directly reading its rendered cover page via the Read tool -- no
  exact publication date was legible on that page, so it is tagged
  `:last-revised-date` with a WebSearch-corroborated 2014 (its last
  known published edition), not primary-page-confirmed. The Our Mission
  (Celebrating 100 Years) profile page was directly WebFetch-verified,
  confirming NAB's 1923 founding in its own text.")

(def catalog
  "assoc-slug -> vector of self-regulatory rule entries."
  {"nab"
   [{:association-rule/id "nab.political-broadcast-catechism"
     :association-rule/title "Political Broadcast Catechism, 16th Edition"
     :association-rule/association "nab"
     :association-rule/isic "6020"
     :association-rule/country "USA"
     :association-rule/kind :best-practices-guide
     :association-rule/url "https://www.nab.org/documents/membership/memberDownloads/Political_Catechism.pdf"
     :association-rule/url-provenance :official-association-site
     :association-rule/last-revised-date "2014"
     :association-rule/retrieved-at "2026-07-15"
     :association-rule/topic #{:political-advertising :disclosure}}
    {:association-rule/id "nab.our-mission-centennial"
     :association-rule/title "Our Mission (Celebrating 100 Years, organization profile)"
     :association-rule/association "nab"
     :association-rule/isic "6020"
     :association-rule/country "USA"
     :association-rule/kind :governance-program
     :association-rule/url "https://www.nab.org/100/nab/ourMission.asp"
     :association-rule/url-provenance :official-association-site
     :association-rule/established-date "1923"
     :association-rule/retrieved-at "2026-07-15"
     :association-rule/topic #{:governance}}]})

(defn spec-basis [assoc-slug] (get catalog assoc-slug))

(defn coverage
  ([] (coverage (keys catalog)))
  ([slugs]
   (let [have (filter catalog slugs)
         missing (remove catalog slugs)]
     {:requested (count slugs)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-6020-usa-nab Wave 0 (ADR-2607141700): "
                 (count (get catalog "nab")) " nab entries seeded with an "
                 "official nab.org citation. Extend "
                 "`association.facts/catalog`, never fabricate a rule id/url.")})))

(defn by-topic [assoc-slug topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis assoc-slug)))
