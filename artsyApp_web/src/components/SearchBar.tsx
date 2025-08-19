import { useState } from "react";
import { Form, Button, InputGroup, Container, Spinner } from "react-bootstrap";
import axios from "axios";

import artsyLogo from "../assets/artsy_logo.svg";

interface Artist {
  id: string;
  name: string;
  image: string;
}

interface SearchBarProps {
  setArtistData: (data: Artist[]) => void;
  clearAll: () => void;
  setSelectedArtistId: React.Dispatch<React.SetStateAction<string | null>>;
  setNoResults: React.Dispatch<React.SetStateAction<boolean>>;
}

const SearchBar = ({
  setArtistData,
  clearAll,
  setSelectedArtistId,
  setNoResults,
}: SearchBarProps) => {
  const [artistName, setArtist] = useState("");
  const [loading, setLoading] = useState(false);

  const search = async () => {
    try {
      setSelectedArtistId(null);
      setLoading(true);
      const { data } = await axios.get(`/api/search/${artistName}`);
      const res = data?._embedded?.results;
      setNoResults(res.length === 0);
      const dispArtists = res.map((artist: any) => {
        const id = artist._links.self?.href.split("/").pop();
        const name = artist.title;
        const imageUrl = artist._links?.thumbnail?.href;

        return {
          id,
          name,
          image: imageUrl.includes("/assets/shared/missing_image.png")
            ? artsyLogo
            : imageUrl,
        };
      });

      setArtistData(dispArtists);
    } catch (error) {
      console.error("Error in fetching the Artists:", error);
    } finally {
      setLoading(false);
    }
  };

  const clearSearch = () => {
    setArtist("");
    setArtistData([]);
    clearAll();
  };

  const handleKeyPress = (event: React.KeyboardEvent) => {
    if (event.key === "Enter") {
      search();
    }
  };

  return (
    <Container fluid className="mt-2">
      <style>
        {`
          @media (max-width: 576px) {
            .searchbar-input,
            .searchbar-button {
              font-size: 0.85rem;
              padding: 0.5rem 0.75rem;
            }

            .searchbar-input::placeholder {
              font-size: 0.85rem;
              white-space: normal;
            }

            .searchbar-wrapper {
              width: 100% !important;
              padding: 0 1rem;
            }
          }

          @media (min-width: 577px) {
            .searchbar-wrapper {
              width: 80%;
            }
          }
        `}
      </style>
      <div className="d-flex justify-content-center">
        <InputGroup
          className="searchbar-wrapper"
          style={{ maxWidth: "1500px" }}
        >
          {" "}
          <Form.Control
            type="text"
            placeholder="Please enter an artist name."
            value={artistName}
            onChange={(e) => setArtist(e.target.value)}
            onKeyDown={handleKeyPress}
            style={{
              flex: 1,
              fontSize: "1rem",
              padding: "0.6rem 1rem",
              minWidth: 0,
              textOverflow: "ellipsis",
              whiteSpace: "nowrap",
              overflow: "hidden",
            }}
          />
          <Button
            style={{
              backgroundColor: "#1E40AF",
              borderColor: "#1E40AF",
              color: "white",
              opacity: loading ? 1 : undefined,
              cursor: loading ? "wait" : "pointer",
            }}
            onClick={search}
            disabled={!artistName || loading}
          >
            Search
            {loading && (
              <Spinner
                as="span"
                animation="border"
                size="sm"
                className="ms-2"
              />
            )}
          </Button>
          <Button
            style={{
              backgroundColor: "grey",
              borderColor: "grey",
              color: "white",
            }}
            onClick={clearSearch}
          >
            Clear
          </Button>
        </InputGroup>
      </div>
    </Container>
  );
};

export default SearchBar;
