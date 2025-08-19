import { Card } from "react-bootstrap";
import star_logo from "../assets/star-regular.svg";
import favStar from "../assets/star-solid.svg";
import { MouseEvent } from "react";

interface CardsArtProps {
  aname: string;
  url: string;
  selected: boolean;
  onSelect: () => void;
  isLoggedIn: boolean;
  isFavorite: boolean;
  onToggleFavorite: () => void;
}

const CardsArt = ({
  aname,
  url,
  selected,
  onSelect,
  isLoggedIn,
  isFavorite,
  onToggleFavorite,
}: CardsArtProps) => {
  const defaultBarColor = "#1E2A37";
  const selectedBarColor = "#17479E";

  const handleStarClick = (e: MouseEvent) => {
    e.stopPropagation();
    if (!isLoggedIn) return;
    onToggleFavorite();
  };

  return (
    <>
      <style>
        {`
          .card-hover:hover .card-body {
            background-color: #17479E !important;
          }
            <Card.Text
              className="card-text mb-0"
              style={{
                whiteSpace: "normal",
                overflowWrap: "break-word",
                fontSize: "0.95rem",
                textAlign: "left",
              }}
            >
        `}
      </style>

      <div style={{ position: "relative", width: "230px", margin: "5px" }}>
        <Card
          className="text-center card-hover"
          style={{
            width: "230px",
            border: "1px solid #ccc",
            borderRadius: "4px",
            cursor: "pointer",
            backgroundColor: "#fff",
          }}
          onClick={onSelect}
        >
          <Card.Img
            variant="top"
            src={url}
            style={{
              height: "230px",
              objectFit: "cover",
              borderTopLeftRadius: "4px",
              borderTopRightRadius: "4px",
            }}
          />
          <Card.Body
            style={{
              backgroundColor: selected ? selectedBarColor : defaultBarColor,
              color: "white",
              padding: "10px",
              borderBottomLeftRadius: "4px",
              borderBottomRightRadius: "4px",
              transition: "background-color 0.3s ease",
            }}
          >
            <Card.Text
              className="card-text mb-0"
              style={{
                whiteSpace: "normal",
                overflowWrap: "break-word",
                fontSize: "0.95rem",
                textAlign: "left",
              }}
            >
              {aname}
            </Card.Text>
          </Card.Body>
        </Card>
        {isLoggedIn && (
          <div
            className="position-absolute top-0 end-0 m-2 bg-primary rounded-circle d-flex align-items-center justify-content-center"
            style={{
              width: "28px",
              height: "28px",
              cursor: "pointer",
              zIndex: 10,
            }}
            onClick={handleStarClick}
          >
            <img
              src={isFavorite ? favStar : star_logo}
              alt="favorite star"
              style={{ width: "16px", height: "16px" }}
            />
          </div>
        )}
      </div>
    </>
  );
};

export default CardsArt;
