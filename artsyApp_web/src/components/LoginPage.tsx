import { useState } from "react";
import { Container, Form, Button } from "react-bootstrap";
import axios from "axios";

interface LoginPageProps {
  onLoginSuccess: (userData: User) => void;
  onSwitchToRegister: () => void;
}

interface User {
  fullName: string;
  imagelink: string;
}

const LoginPage = ({ onLoginSuccess, onSwitchToRegister }: LoginPageProps) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [selectedfield_Email, setselectedfield_Email] = useState(false);
  const [selectedfield_Password, setselectedfield_Password] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");

  const emailRegex = /\S+@\S+\.\S+/.test(email);
  const passCheck = password.trim() !== "";
  const valid = emailRegex && passCheck;

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErrorMsg("");
    try {
      const res = await axios.post("/api/login/", { email, password });
      console.log(res.data);
      const { user } = res.data;
      onLoginSuccess(user);
      console.log(user);
    } catch (err: any) {
      setErrorMsg("Password or email is incorrect");
    }
  }

  return (
    <>
      <Container
        style={{
          maxWidth: "400px",
          marginTop: "2rem",
          border: "1px solid #ddd",
          borderRadius: "8px",
          padding: "1.5rem",
          backgroundColor: "#fff",
        }}
      >
        <h2
          className="mb-4"
          style={{ fontWeight: "normal", textAlign: "left" }}
        >
          Login
        </h2>
        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3" controlId="loginEmail">
            <Form.Label>Email address</Form.Label>
            <Form.Control
              type="email"
              placeholder="Enter email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              onBlur={() => setselectedfield_Email(true)}
              isInvalid={selectedfield_Email && !emailRegex}
              required
            />
            <Form.Control.Feedback type="invalid">
              Email must be valid.
            </Form.Control.Feedback>
          </Form.Group>

          <Form.Group className="mb-3" controlId="loginPassword">
            <Form.Label>Password</Form.Label>
            <Form.Control
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onBlur={() => setselectedfield_Password(true)}
              isInvalid={selectedfield_Password && !passCheck}
              required
            />
            <Form.Control.Feedback type="invalid">
              Password is required.
            </Form.Control.Feedback>
          </Form.Group>
          {errorMsg && (
            <div className="text-danger mb-3" style={{ fontSize: "0.9rem" }}>
              {errorMsg}
            </div>
          )}
          <div className="d-grid">
            <Button
              variant="primary"
              type="submit"
              disabled={!valid}
              style={{
                backgroundColor: valid ? "#17479E" : "#6c87b9",
                borderColor: valid ? "#17479E" : "#6c87b9",
                cursor: valid ? "pointer" : "not-allowed",
              }}
            >
              Log in
            </Button>
          </div>
        </Form>
      </Container>
      <p className="text-center mt-3" style={{ fontSize: "0.95rem" }}>
        Don’t have an account yet?{" "}
        <span
          onClick={onSwitchToRegister}
          style={{
            color: "#0d6efd",
            cursor: "pointer",
            textDecoration: "underline",
          }}
        >
          Register
        </span>
      </p>
    </>
  );
};

export default LoginPage;
