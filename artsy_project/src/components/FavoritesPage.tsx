import { useEffect, useState } from "react";
import { Spinner, Row, Col, Card } from "react-bootstrap";
import axios from "axios";
import moment from "moment";
import artsyLogo from "../assets/artsy_logo.svg";

interface FavoriteEntry {
  name: string;
  birthday: string;
  deathday: string;
  nationality: string;
  image: string;
  DateTimeAdded: string;
}

interface FavoriteData {
  [id: string]: FavoriteEntry;
}

interface FavoritesPageProps {
  triggerToast: (message: string, type: "success" | "danger") => void;
  onSelectArtistFromFavorites: (artistId: string) => void;
}

const FavoritesPage = ({
  triggerToast,
  onSelectArtistFromFavorites,
}: FavoritesPageProps) => {
  const [favorites, setFavorites] = useState<FavoriteData>({});
  const [loading, setLoading] = useState(true);
  const [tick, setTick] = useState(0);

  const isMobile = window.innerWidth <= 576;

  useEffect(() => {
    axios.post("/api/getFav/").then((res) => {
      setFavorites(res.data.favorites || {});
      setLoading(false);
    });
  }, []);

  useEffect(() => {
    const interval = setInterval(() => {
      setTick((prev) => prev + 1);
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  const handleRemove = async (artistId: string) => {
    try {
      const res = await axios.post("/api/remFav/", { artistId });
      setFavorites(res.data.favorites);
      triggerToast("Removed from favorites", "danger");
    } catch (err) {
      console.log(" ", err);
    }
  };
  const timeDiff = (dateString: string): string => {
    const now = moment();
    const then = moment(dateString);
    const seconds = now.diff(then, "seconds");
    const minutes = now.diff(then, "minutes");
    const hours = now.diff(then, "hours");

    if (seconds < 60) {
      return `${seconds} second${seconds === 1 ? "" : "s"} ago`;
    } else if (minutes < 60) {
      return `${minutes} minute${minutes === 1 ? "" : "s"} ago`;
    } else if (hours < 24) {
      return `${hours} hour${hours === 1 ? "" : "s"} ago`;
    } else {
      return then.fromNow();
    }
  };

  if (loading)
    return (
      <div className="text-center mt-4">
        <Spinner animation="border" />
      </div>
    );

  const favoriteEntries = Object.entries(favorites).sort(
    ([, a], [, b]) =>
      new Date(b.DateTimeAdded).getTime() - new Date(a.DateTimeAdded).getTime()
  );

  if (favoriteEntries.length === 0)
    return (
      <div className="d-flex justify-content-center mt-4">
        <div
          className="alert alert-danger"
          role="alert"
          style={{
            margin: 0,
            padding: "10px 15px",
            fontSize: "1rem",
            textAlign: "left",
            width: "80%",
            maxWidth: "1500px",
          }}
        >
          No favorite artists.
        </div>
      </div>
    );
  return (
    <div className="d-flex justify-content-center mt-4">
      <div style={{ width: isMobile ? "90%" : "70%", maxWidth: "1500px" }}>
        <Row>
          {favoriteEntries.map(([id, artist]) => {
            const isFallback = artist.image.includes(
              "/assets/shared/missing_image.png"
            );
            const imageToUse = isFallback ? artsyLogo : artist.image;

            return (
              <Col
                key={id}
                xs={12}
                sm={12}
                md={6}
                lg={4}
                xl={4}
                className="mb-3 d-flex justify-content-center"
              >
                <Card
                  className="text-white position-relative"
                  style={{
                    height: "150px",
                    width: "400px",
                    borderRadius: "12px",
                    overflow: "hidden",
                  }}
                >
                  <img
                    src={imageToUse}
                    alt={artist.name}
                    style={{
                      position: "absolute",
                      top: 0,
                      left: 0,
                      height: "100%",
                      width: "100%",
                      objectFit: "cover",
                      filter: "blur(6px)",
                      zIndex: 1,
                    }}
                  />

                  <div
                    className="position-absolute top-0 start-0 w-100 h-100"
                    style={{
                      backgroundColor: "rgba(0, 0, 0, 0.4)",
                      zIndex: 2,
                    }}
                  />

                  <div
                    className="position-absolute top-0 start-0 w-100 h-100 px-3 py-2 d-flex flex-column justify-content-between"
                    style={{ zIndex: 3, cursor: "pointer" }}
                    onClick={(e) => {
                      if ((e.target as HTMLElement).innerText !== "Remove") {
                        onSelectArtistFromFavorites(id);
                      }
                    }}
                  >
                    <div>
                      <h5 className="mb-1">{artist.name}</h5>
                      <div className="small">
                        {artist.birthday} - {artist.deathday}
                        <br />
                        {artist.nationality}
                      </div>
                    </div>
                    <div className="small">
                      {timeDiff(artist.DateTimeAdded)}
                      <span
                        className="ms-2 text-decoration-underline float-end"
                        style={{ cursor: "pointer" }}
                        onClick={() => handleRemove(id)}
                      >
                        Remove
                      </span>
                    </div>
                  </div>
                </Card>
              </Col>
            );
          })}
        </Row>
      </div>
    </div>
  );
};

export default FavoritesPage;
