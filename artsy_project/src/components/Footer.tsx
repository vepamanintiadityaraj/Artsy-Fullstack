import { Container } from "react-bootstrap";
import artsy_logo from "../assets/artsy_logo.svg";

function Footer() {
  function navArtsy() {
    window.location.href = "https://www.artsy.net";
  }
  return (
    <footer className="bg-dark text-white text-center py-2 fixed-bottom">
      <Container>
        <span onClick={navArtsy} style={{ cursor: "pointer" }}>
          Powered by{" "}
          <img
            src={artsy_logo}
            style={{ width: "20px", height: "20px", verticalAlign: "middle" }}
          />{" "}
          Artsy.
        </span>
      </Container>
    </footer>
  );
}

export default Footer;
