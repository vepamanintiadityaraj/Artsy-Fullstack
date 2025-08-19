import { useState } from "react";
import { Button, Container } from "react-bootstrap";
import ArtistInfo from "./ArtistInfo";
import Artworks from "./ArtWorks";
import SimilarArtists from "./SimilarArtists";

interface OptionBoxProps {
  artistId: string | null;
  isLoggedIn: boolean;
  onSelectArtist: (artist: { id: string; name: string; image: string }) => void;
  favorites: { [id: string]: any };
  setFavorites: React.Dispatch<React.SetStateAction<any>>;
  triggerToast: (message: string, type: "success" | "danger") => void;
}
const OptionBox = ({
  artistId,
  isLoggedIn,
  onSelectArtist,
  favorites,
  setFavorites,
  triggerToast,
}: OptionBoxProps) => {
  if (!artistId) return null;

  const [selectedTab, setSelectedTab] = useState<"artistInfo" | "artworks">(
    "artistInfo"
  );
  const isMobile = window.innerWidth <= 768;

  return (
    <>
      <Container
        className="mt-3"
        style={{ width: isMobile ? "90%" : "80%", maxWidth: "1500px" }}
      >
        <div
          style={{
            display: "flex",
            border: "none",
            borderRadius: "4px",
            overflow: "hidden",
          }}
        >
          <Button
            style={{
              flex: 1,
              borderRadius: 0,
              backgroundColor:
                selectedTab === "artistInfo" ? "#17479E" : "transparent",
              color: selectedTab === "artistInfo" ? "#fff" : "#3B82F6",
              border: "none",
            }}
            onClick={() => setSelectedTab("artistInfo")}
          >
            Artist Info
          </Button>

          <Button
            style={{
              flex: 1,
              borderRadius: 0,
              backgroundColor:
                selectedTab === "artworks" ? "#17479E" : "transparent",
              color: selectedTab === "artworks" ? "#fff" : "#3B82F6",
              border: "none",
            }}
            onClick={() => setSelectedTab("artworks")}
          >
            Artworks
          </Button>
        </div>
        {selectedTab === "artistInfo" && (
          <ArtistInfo
            artistId={artistId}
            isLoggedIn={isLoggedIn}
            favorites={favorites}
            setFavorites={setFavorites}
            triggerToast={triggerToast}
          />
        )}
        {selectedTab === "artworks" && <Artworks artistId={artistId} />}
      </Container>
      {isLoggedIn && (
        <SimilarArtists
          artistId={artistId}
          isLoggedIn={isLoggedIn}
          onSelectArtist={onSelectArtist}
          favorites={favorites}
          setFavorites={setFavorites}
          triggerToast={triggerToast}
        />
      )}
    </>
  );
};

export default OptionBox;
