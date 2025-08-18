import { useState, useEffect } from "react";
import { Container, Row, Col, Card, Spinner, Button } from "react-bootstrap";
import axios from "axios";
import CategoriesModal from "./CategoriesModal";

interface ArtworksProps {
  artistId: string;
}

interface Artwork {
  id: string;
  title: string;
  date: string;
  thumbnail: string;
}

const Artworks = ({ artistId }: ArtworksProps) => {
  const [artworks, setArtworks] = useState<Artwork[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [showModal, setShowModal] = useState(false);
  const [selectedArtwork, setSelectedArtwork] = useState<Artwork | null>(null);

  useEffect(() => {
    setArtworks([]);
    setError("");
    setLoading(true);

    const fetchArtworks = async () => {
      try {
        const { data } = await axios.get(`/api/artworks/${artistId}`);

        const results = data._embedded?.artworks || [];
        if (results.length === 0) {
          setError("No artworks.");
        } else {
          const processed = results.map((art: any) => ({
            id: art.id,
            title: art.title || "Untitled",
            date: art.date || "",
            thumbnail: art._links?.thumbnail?.href || "",
          }));
          setArtworks(processed);
        }
      } catch (err) {
        console.error("Error fetching artworks:", err);
        setError("No artworks.");
      } finally {
        setLoading(false);
      }
    };

    fetchArtworks();
  }, [artistId]);

  if (loading) {
    return (
      <Container className="text-center mt-3">
        <Spinner animation="border" />
      </Container>
    );
  }

  if (error) {
    return (
      <Container className="mt-4">
        <div className="w-100 d-flex justify-content-center">
          <div style={{ width: "100%", maxWidth: "1500px" }}>
            <div
              className="alert alert-danger"
              role="alert"
              style={{
                margin: 0,
                padding: "10px 15px",
                fontSize: "1rem",
                textAlign: "left",
              }}
            >
              {error}
            </div>
          </div>
        </div>
      </Container>
    );
  }
  const handleViewCategories = (art: Artwork) => {
    setSelectedArtwork(art);
    setShowModal(true);
  };

  return (
    <Container className="mt-3">
      <Row>
        {artworks.map((art) => (
          <Col key={art.id} xs={12} sm={6} md={4} lg={3}>
            <Card className="mb-3 border border-secondary-subtle rounded-2 shadow-none">
              <Card.Img
                variant="top"
                src={art.thumbnail}
                style={{
                  width: "100%",
                  height: "auto",
                  objectFit: "contain",
                }}
              />
              <Card.Body className="text-center" style={{ padding: "10px 0" }}>
                <Card.Title style={{ fontSize: "1rem", fontWeight: "normal" }}>
                  {art.title}
                  {art.date ? `, ${art.date}` : ""}
                </Card.Title>
                <Button
                  variant="light"
                  style={{
                    width: "100%",
                    border: "none",
                    fontWeight: "normal",

                    backgroundColor: "#f8f9fa",
                    marginBottom: "0",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = "#17479E";
                    e.currentTarget.style.color = "#fff";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = "#f8f9fa";
                    e.currentTarget.style.color = "#000";
                  }}
                  onClick={() => handleViewCategories(art)}
                >
                  View categories
                </Button>
              </Card.Body>
            </Card>
          </Col>
        ))}
      </Row>
      {selectedArtwork && (
        <CategoriesModal
          show={showModal}
          onHide={() => setShowModal(false)}
          artworkId={selectedArtwork.id}
          artworkTitle={selectedArtwork.title}
          artworkDate={selectedArtwork.date}
          artworkThumbnail={selectedArtwork.thumbnail}
        />
      )}
    </Container>
  );
};

export default Artworks;
