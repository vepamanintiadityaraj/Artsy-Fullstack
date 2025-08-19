import { useState } from "react";
import { Row, Col } from "react-bootstrap";
import CardsArt from "./CardsArt";
import axios from "axios";

interface Artist {
  id: string;
  name: string;
  image: string;
}

interface FavoriteData {
  [id: string]: {
    name: string;
    birthday: string;
    deathday: string;
    nationality: string;
    image: string;
    DateTimeAdded: string;
  };
}

interface CardDetails {
  artists: Artist[];
  onSelectArtist: (artistId: Artist) => void;
  isLoggedIn: boolean;
  favorites: FavoriteData;
  setFavorites: React.Dispatch<React.SetStateAction<FavoriteData>>;
  triggerToast: (message: string, type: "success" | "danger") => void;
}

const CardList = ({
  artists,
  onSelectArtist,
  isLoggedIn,
  favorites,
  setFavorites,
  triggerToast,
}: CardDetails) => {
  const [selectedID, setSelectedID] = useState<string | null>(null);
  const isMobile = window.innerWidth <= 768;

  return (
    <div className="container-fluid mt-2 d-flex justify-content-center">
      <div
        className="overflow-auto"
        style={{
          width: isMobile ? "90%" : "80%",
          maxWidth: "1500px",
          margin: "0 auto",
        }}
      >
        <Row className="flex-nowrap gx-2">
          {artists.map((artist) => (
            <Col key={artist.id} xs="auto">
              <CardsArt
                aname={artist.name}
                url={artist.image}
                selected={selectedID === artist.id}
                isLoggedIn={isLoggedIn}
                isFavorite={!!favorites[artist.id]}
                onToggleFavorite={async () => {
                  if (!isLoggedIn) return;

                  const alreadyFav = !!favorites[artist.id];

                  if (alreadyFav) {
                    const res = await axios.post("/api/remFav/", {
                      artistId: artist.id,
                    });
                    setFavorites(res.data.favorites);
                    triggerToast("Removed from favorites", "danger");
                  } else {
                    try {
                      const { data } = await axios.get(`/api/end/${artist.id}`);

                      const payload = {
                        id: artist.id,
                        name: data.name || "",
                        birthday: data.birthday || "",
                        deathday: data.deathday || "",
                        nationality: data.nationality || "",
                        image: artist.image,
                      };

                      const res = await axios.post("/api/addFav/", {
                        artist: payload,
                      });
                      triggerToast("Added to favorites", "success");
                      setFavorites(res.data.favorites);
                    } catch (err) {
                      console.error(
                        "Failed to fetch artist details before adding favorite",
                        err
                      );
                    }
                  }
                }}
                onSelect={() => {
                  setSelectedID(artist.id);
                  onSelectArtist(artist);
                }}
              />
            </Col>
          ))}
        </Row>
      </div>
    </div>
  );
};

export default CardList;
