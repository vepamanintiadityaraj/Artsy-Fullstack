import { useEffect, useState } from "react";
import { Modal, Spinner, Container, Row, Col, Card } from "react-bootstrap";
import axios from "axios";

interface Category {
  id: string;
  name: string;
  thumbnail: string;
}

interface CategoriesModalProps {
  show: boolean;
  onHide: () => void;
  artworkId: string;
  artworkTitle: string;
  artworkDate: string;
  artworkThumbnail: string;
}

const CategoriesModal = ({
  show,
  onHide,
  artworkId,
  artworkTitle,
  artworkDate,
  artworkThumbnail,
}: CategoriesModalProps) => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 576);
    };
    handleResize();
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    const closeBtn = document.querySelector(".btn-close") as HTMLElement;
    if (closeBtn) {
      closeBtn.style.border = "1px solid #d1d5db";
      closeBtn.style.borderRadius = "6px";
      closeBtn.style.padding = "6px";
      closeBtn.style.width = "34px";
      closeBtn.style.height = "34px";
      closeBtn.style.display = "flex";
      closeBtn.style.alignItems = "center";
      closeBtn.style.justifyContent = "center";
    }
  }, [show]);

  useEffect(() => {
    if (!show) return;

    const fetchCategories = async () => {
      try {
        setLoading(true);
        setError("");

        const { data } = await axios.get(`/api/genes/${artworkId}`);

        const results = data._embedded?.genes || [];
        if (results.length === 0) {
          setError("No categories.");
        } else {
          const mapped = results.map((gene: any) => ({
            id: gene.id,
            name: gene.name || "Unnamed",
            thumbnail: gene._links?.thumbnail?.href || "",
          }));
          setCategories(mapped);
        }
      } catch (err) {
        console.error("Error fetching categories:", err);
        setError("No categories.");
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, [show, artworkId]);

  const HeaderContent = () => (
    <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
      <img
        src={artworkThumbnail}
        alt={artworkTitle}
        style={{
          width: "65px",
          height: "65px",
          objectFit: "cover",
          borderRadius: "4px",
        }}
      />
      <div>
        <div
          style={{
            fontSize: "1rem",
            fontWeight: "normal",
            marginBottom: "2px",
          }}
        >
          {artworkTitle}
        </div>
        {artworkDate && (
          <div style={{ fontSize: "0.85rem" }}>{artworkDate}</div>
        )}
      </div>
    </div>
  );
  <style>
    {`
    .btn-close {
      border: 1px solid #d1d5db;
      border-radius: 6px;
      padding: 6px;
      width: 34px;
      height: 34px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  `}
  </style>;

  return (
    <Modal
      show={show}
      onHide={onHide}
      size="xl"
      dialogClassName="custom-modal-top"
    >
      <Modal.Header
        closeButton
        style={{ borderBottom: "none", paddingRight: "16px" }}
      >
        <Modal.Title>
          <HeaderContent />
        </Modal.Title>
      </Modal.Header>

      <hr style={{ margin: 0 }} />

      <Modal.Body style={{ minHeight: "200px" }}>
        {loading ? (
          <div className="text-center">
            <Spinner animation="border" />
          </div>
        ) : error ? (
          <div className="text-center">
            <p>{error}</p>
          </div>
        ) : (
          <Container>
            <Row>
              {categories.map((cat) => (
                <Col key={cat.id} xs={12} sm={6} md={4} lg={3}>
                  <Card
                    className="mb-3"
                    style={{ borderRadius: "4px", border: "1px solid #ddd" }}
                  >
                    <Card.Img
                      variant="top"
                      src={cat.thumbnail}
                      style={{
                        height: isMobile ? "400px" : "220px",
                        objectFit: "cover",
                      }}
                    />
                    <Card.Body
                      className="text-center"
                      style={{ padding: "10px" }}
                    >
                      <Card.Text
                        style={{
                          margin: 0,
                          fontSize: "0.9rem",
                          fontWeight: "normal",
                        }}
                      >
                        {cat.name}
                      </Card.Text>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          </Container>
        )}
      </Modal.Body>
    </Modal>
  );
};

export default CategoriesModal;
