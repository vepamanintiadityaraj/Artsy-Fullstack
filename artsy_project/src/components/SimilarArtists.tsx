import { useEffect, useState } from "react";
import { Row, Col, Spinner } from "react-bootstrap";
import axios from "axios";
import CardsArt from "./CardsArt";

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

interface SimilarArtistsProps {
  artistId: string;
  isLoggedIn: boolean;
  onSelectArtist: (artist: Artist) => void;
  favorites: FavoriteData;
  setFavorites: React.Dispatch<React.SetStateAction<FavoriteData>>;
  triggerToast: (message: string, type: "success" | "danger") => void;
}

const SimilarArtists = ({
  artistId,
  isLoggedIn,
  onSelectArtist,
  favorites,
  setFavorites,
  triggerToast,
}: SimilarArtistsProps) => {
  const [similarArtists, setSimilarArtists] = useState<Artist[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedID, setSelectedID] = useState<string | null>(null);

  useEffect(() => {
    const fetchSimilarArtists = async () => {
      try {
        const res = await axios.get(`/api/artists/${artistId}`);
        const mapped =
          res.data._embedded?.artists.map((artist: any) => ({
            id: artist.id,
            name: artist.name,
            image:
              artist._links?.image?.href?.replace(
                "{image_version}",
                "square"
              ) || "",
          })) || [];

        setSimilarArtists(mapped);
      } catch (err) {
        console.error("Error fetching similar artists", err);
      } finally {
        setLoading(false);
      }
    };

    fetchSimilarArtists();
  }, [artistId]);

  if (loading) return null;

  if (similarArtists.length === 0) return null;

  return (
    <div className="mt-4">
      <div className="d-flex justify-content-center">
        <div
          className="overflow-auto"
          style={{
            width: "80%",
            maxWidth: "1500px",
            whiteSpace: "nowrap",
            position: "relative",
          }}
        >
          <div
            style={{
              position: "sticky",
              left: 0,
              backgroundColor: "white",
              zIndex: 10,
              paddingBottom: "8px",
            }}
          >
            <h4 className="mb-3" style={{ marginBottom: "0.5rem" }}>
              Similar Artists
            </h4>
          </div>
          <Row className="flex-nowrap g-2">
            {similarArtists.map((artist) => (
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
                        const { data } = await axios.get(
                          `/api/end/${artist.id}`
                        );
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
                        setFavorites(res.data.favorites);
                        triggerToast("Added to favorites", "success");
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
    </div>
  );
};

export default SimilarArtists;
