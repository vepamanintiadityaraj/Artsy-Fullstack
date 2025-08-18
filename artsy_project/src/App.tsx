import Header from "./components/Header";
import Footer from "./components/Footer";
import SearchBar from "./components/SearchBar";
import CardList from "./components/CardList";
import OptionBox from "./components/OptionBox";
import LoginPage from "./components/LoginPage";
import RegisterPage from "./components/RegisterPage";
import { ToastContainer } from "react-toastify";
import FavoritesPage from "./components/FavoritesPage";
import CustomToast from "./components/Toasts";
import "react-toastify/dist/ReactToastify.css";

import { useState, useEffect } from "react";
import axios from "axios";
axios.defaults.withCredentials = true;

interface Artist {
  id: string;
  name: string;
  image: string;
}

interface User {
  fullName: string;
  imagelink: string;
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

function App() {
  const [artistData, setArtistData] = useState<Artist[]>([]);
  const [selectedArtistId, setSelectedArtistId] = useState<string | null>(null);
  const [selectedTab, setSelectedTab] = useState<
    "Search" | "Login" | "Register" | "Favorites"
  >("Search");
  const [noResults, setNoResults] = useState(false);

  const [user, setUser] = useState<User | null>(null);
  const [favorites, setFavorites] = useState<FavoriteData>({});

  const [toastStack, setToastStack] = useState<
    { message: string; type: "success" | "danger" }[]
  >([]);

  const triggerToast = (message: string, type: "success" | "danger") => {
    const toast = { message, type };
    setToastStack((prev) => [...prev, toast]);

    setTimeout(() => {
      setToastStack((prev) => prev.filter((t) => t !== toast));
    }, 3000);
  };

  const resetSearchPage = () => {
    setSelectedTab("Search");
    setArtistData([]);
    setSelectedArtistId(null);
    setNoResults(false);

    sessionStorage.removeItem("artistData");
    sessionStorage.removeItem("selectedArtistId");
  };

  const clearAll = () => {
    setArtistData([]);
    setSelectedArtistId(null);
    sessionStorage.removeItem("artistData");
    sessionStorage.removeItem("selectedArtistId");
  };

  const handleLoginSuccess = (loggedInUser: User) => {
    sessionStorage.clear();
    resetSearchPage();
    setUser(loggedInUser);
  };

  const handleLogout = async () => {
    try {
      await axios.post("/api/logout/");
      sessionStorage.clear();
      resetSearchPage();
      setUser(null);
      triggerToast("Logged Out", "success");
    } catch (error) {
      console.error("Logout error:", error);
    }
  };

  const handleDeleteAccount = async () => {
    try {
      await axios.delete("/api/del/");
      sessionStorage.clear();
      resetSearchPage();
      setUser(null);
      triggerToast("Account Deleted", "danger");
    } catch (error) {
      console.error("Delete account error:", error);
    }
  };

  const handleTabChange = async (
    tab: "Search" | "Login" | "Register" | "Favorites"
  ) => {
    setSelectedTab(tab);
    if (tab === "Search" && user) {
      try {
        const res = await axios.get("/api/me");
        setFavorites(res.data.user.favorites || {});
      } catch (err) {
        console.error("Failed to refresh favorites:", err);
      }
    }
  };

  useEffect(() => {
    const storedTab = sessionStorage.getItem("selectedTab");
    if (storedTab) setSelectedTab(storedTab as typeof selectedTab);

    const storedArtists = sessionStorage.getItem("artistData");
    const storedSelected = sessionStorage.getItem("selectedArtistId");

    if (storedArtists) setArtistData(JSON.parse(storedArtists));
    if (storedSelected) setSelectedArtistId(storedSelected);

    const fetchUser = async () => {
      try {
        const res = await axios.get("/api/me");
        setUser(res.data.user);
        setFavorites(res.data.user.favorites || {});
      } catch {
        setUser(null);
        setFavorites({});
      }
    };

    fetchUser();
  }, []);

  useEffect(() => {
    sessionStorage.setItem("selectedTab", selectedTab);
  }, [selectedTab]);

  useEffect(() => {
    sessionStorage.setItem("artistData", JSON.stringify(artistData));
  }, [artistData]);

  useEffect(() => {
    if (selectedArtistId) {
      sessionStorage.setItem("selectedArtistId", selectedArtistId);
    } else {
      sessionStorage.removeItem("selectedArtistId");
    }
  }, [selectedArtistId]);

  return (
    <div style={{ paddingBottom: "80px" }}>
      {" "}
      <Header
        user={user}
        selectedTab={selectedTab}
        setSelectedTab={handleTabChange}
        onLogout={handleLogout}
        onDeleteAccount={handleDeleteAccount}
      />
      <div
        style={{
          position: "absolute",
          top: 70,
          right: 20,
          zIndex: 9999,
          display: "flex",
          flexDirection: "column",
          alignItems: "flex-end",
          gap: "10px",
        }}
      >
        {toastStack.map((toast, index) => (
          <CustomToast
            key={index}
            show={true}
            message={toast.message}
            type={toast.type}
            onClose={() =>
              setToastStack((prev) => prev.filter((_, i) => i !== index))
            }
          />
        ))}
      </div>
      {selectedTab === "Search" && (
        <>
          <SearchBar
            setArtistData={setArtistData}
            clearAll={clearAll}
            setSelectedArtistId={setSelectedArtistId}
            setNoResults={setNoResults}
          />
          {noResults && (
            <div className="d-flex justify-content-center mt-3">
              <div
                className="alert alert-dark text-start"
                role="alert"
                style={{ width: "80%", maxWidth: "1500px" }}
              >
                No results.
              </div>
            </div>
          )}
          <div className="mt-4 d-flex justify-content-center">
            <CardList
              artists={artistData}
              onSelectArtist={(artist) => setSelectedArtistId(artist.id)}
              isLoggedIn={!!user}
              favorites={favorites}
              setFavorites={setFavorites}
              triggerToast={triggerToast}
            />
          </div>
          {selectedArtistId && (
            <OptionBox
              artistId={selectedArtistId}
              isLoggedIn={!!user}
              onSelectArtist={(artist) => setSelectedArtistId(artist.id)}
              favorites={favorites}
              setFavorites={setFavorites}
              triggerToast={triggerToast}
            />
          )}
        </>
      )}
      {selectedTab === "Favorites" && (
        <FavoritesPage
          triggerToast={triggerToast}
          onSelectArtistFromFavorites={(artistId) => {
            setSelectedTab("Search");
            setArtistData([]);
            setSelectedArtistId(artistId);
          }}
        />
      )}
      {selectedTab === "Login" && (
        <LoginPage
          onLoginSuccess={handleLoginSuccess}
          onSwitchToRegister={() => setSelectedTab("Register")}
        />
      )}
      {selectedTab === "Register" && (
        <RegisterPage
          onRegisterSuccess={(user) => {
            sessionStorage.clear();
            resetSearchPage();
            setUser(user);
          }}
          onSwitchToLogin={() => setSelectedTab("Login")}
        />
      )}
      <Footer />
      <ToastContainer
        position="top-right"
        autoClose={3000}
        theme="colored"
        style={{ zIndex: 9999 }}
      />
    </div>
  );
}

export default App;
