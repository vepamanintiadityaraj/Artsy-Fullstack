import { Container, Navbar, Nav, Button, Dropdown } from "react-bootstrap";

interface HeaderProps {
  user: User | null;
  selectedTab: "Search" | "Login" | "Register" | "Favorites";
  setSelectedTab: (tab: "Search" | "Login" | "Register" | "Favorites") => void;
  onLogout: () => void;
  onDeleteAccount: () => void;
}

interface User {
  fullName: string;
  imagelink: string;
}

function Header({
  user,
  selectedTab,
  setSelectedTab,
  onLogout,
  onDeleteAccount,
}: HeaderProps) {
  return (
    <Navbar bg="light" expand="lg" className="w-100">
      <Container fluid className="px-4">
        <Navbar.Brand href="/" style={{ fontSize: "1.2rem" }}>
          Artist Search
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav" className="w-100">
          <Nav className="ms-auto d-flex flex-column flex-lg-row align-items-stretch align-items-lg-center">
            <Button
              className="w-100 w-lg-auto mb-2 mb-lg-0 text-start"
              style={{
                backgroundColor:
                  selectedTab === "Search" ? "#17479E" : "transparent",
                color: selectedTab === "Search" ? "#fff" : "#000",
                border: "none",
                boxShadow: "none",
              }}
              onClick={() => setSelectedTab("Search")}
            >
              Search
            </Button>

            {user ? (
              <>
                <Button
                  className="w-100 w-lg-auto mb-2 mb-lg-0 text-start"
                  style={{
                    backgroundColor:
                      selectedTab === "Favorites" ? "#17479E" : "transparent",
                    color: selectedTab === "Favorites" ? "#fff" : "#000",
                    border: "none",
                    boxShadow: "none",
                  }}
                  onClick={() => setSelectedTab("Favorites")}
                >
                  Favorites
                </Button>
                <div className="w-100 w-lg-auto d-flex justify-content-center justify-content-lg-end">
                  <Dropdown align="end">
                    <Dropdown.Toggle
                      variant="light"
                      id="profile-dropdown"
                      className="d-flex align-items-center"
                    >
                      <img
                        src={user.imagelink}
                        alt="avatar"
                        style={{
                          width: "32px",
                          height: "32px",
                          borderRadius: "50%",
                          marginRight: "8px",
                        }}
                      />
                      {user.fullName}
                    </Dropdown.Toggle>
                    <Dropdown.Menu
                      className="shadow"
                      style={{
                        position: "absolute",
                        top: "100%",
                        right: 0,
                        zIndex: 9999,
                      }}
                    >
                      <Dropdown.Item onClick={onDeleteAccount}>
                        Delete account
                      </Dropdown.Item>
                      <Dropdown.Item onClick={onLogout}>Log out</Dropdown.Item>
                    </Dropdown.Menu>
                  </Dropdown>
                </div>
              </>
            ) : (
              <>
                <Button
                  className="w-100 w-lg-auto mb-2 mb-lg-0 text-start"
                  style={{
                    backgroundColor:
                      selectedTab === "Login" ? "#17479E" : "transparent",
                    color: selectedTab === "Login" ? "#fff" : "#000",
                    border: "none",
                    boxShadow: "none",
                  }}
                  onClick={() => setSelectedTab("Login")}
                >
                  Login
                </Button>
                <Button
                  className="w-100 w-lg-auto mb-2 mb-lg-0 text-start"
                  style={{
                    backgroundColor:
                      selectedTab === "Register" ? "#17479E" : "transparent",
                    color: selectedTab === "Register" ? "#fff" : "#000",
                    border: "none",
                    boxShadow: "none",
                  }}
                  onClick={() => setSelectedTab("Register")}
                >
                  Register
                </Button>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}

export default Header;
