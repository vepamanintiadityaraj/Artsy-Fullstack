import { useState, useEffect } from "react";
import { Container, Spinner } from "react-bootstrap";
import axios from "axios";
import star_logo from "../assets/star-regular.svg";
import favStar from "../assets/star-solid.svg";
import { MouseEvent as ReactMouseEvent } from "react";

interface ArtistInfoProps {
  artistId: string;
  isLoggedIn: boolean;
  favorites: { [id: string]: any };
  setFavorites: React.Dispatch<React.SetStateAction<any>>;
  triggerToast: (message: string, type: "success" | "danger") => void;
}

interface ArtistDetails {
  name: string;
  birthday: string;
  deathday: string;
  nationality: string;
  biography: string;
  image: string;
}

const ArtistInfo = ({
  artistId,
  isLoggedIn,
  favorites,
  setFavorites,
  triggerToast,
}: ArtistInfoProps) => {
  const [artist, setArtist] = useState<ArtistDetails | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchArtistDetails = async () => {
      try {
        const { data } = await axios.get(`/api/end/${artistId}`);

        setArtist({
          name: data.name || "",
          birthday: data.birthday || "",
          deathday: data.deathday || "",
          nationality: data.nationality || "",
          biography: data.biography || "",
          image:
            data._links?.image?.href?.replace("{image_version}", "square") ||
            "",
        });
      } catch (error) {
        console.error("Error fetching artist details:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchArtistDetails();
  }, [artistId]);

  if (loading)
    return (
      <Container className="text-center mt-3">
        <Spinner animation="border" />
      </Container>
    );

  const isFavorite = !!favorites[artistId];

  const handleStarClick = async (e: ReactMouseEvent<HTMLImageElement>) => {
    e.stopPropagation();
    if (!isLoggedIn || !artist) return;

    try {
      if (isFavorite) {
        const res = await axios.post("/api/remFav/", {
          artistId: artistId,
        });
        setFavorites(res.data.favorites);
        triggerToast("Removed from favorites", "danger");
      } else {
        const res = await axios.post("/api/addFav/", {
          artist: {
            id: artistId,
            name: artist.name,
            birthday: artist.birthday,
            deathday: artist.deathday,
            nationality: artist.nationality,
            image: artist.image,
          },
        });
        setFavorites(res.data.favorites);
        triggerToast("Added to favorites", "success");
      }
    } catch (err) {
      console.error("Favorite toggle failed:", err);
    }
  };
  const textChange = (text: string): string => {
    let newText = text;
    newText = newText.replace(/(\w+)-\s+(\w+)/g, "$1$2");
    return newText;
  };

  if (loading)
    return (
      <Container className="text-center mt-3">
        <Spinner animation="border" />
      </Container>
    );

  return (
    <Container className="mt-4">
      <h2 className="d-flex justify-content-center align-items-center gap-2 text-center">
        {artist?.name}
        {isLoggedIn && (
          <img
            src={isFavorite ? favStar : star_logo}
            alt="favorite star"
            width={24}
            height={24}
            role="button"
            onClick={handleStarClick}
          />
        )}
      </h2>
      <p className="text-center">
        {artist?.nationality && `${artist.nationality}, `}
        {artist?.birthday}
        {" – "}
        {artist?.deathday}
      </p>
      <p style={{ textAlign: "justify", whiteSpace: "pre-wrap" }}>
        {textChange(artist?.biography || "")}
      </p>
    </Container>
  );
};

export default ArtistInfo;
