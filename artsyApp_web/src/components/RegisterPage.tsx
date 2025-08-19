import { useState } from "react";
import { Container, Form, Button } from "react-bootstrap";
import axios from "axios";

interface RegisterPageProps {
  onRegisterSuccess: (userData: User) => void;
  onSwitchToLogin: () => void;
}

interface User {
  fullName: string;
  imagelink: string;
}

function RegisterPage({
  onRegisterSuccess,
  onSwitchToLogin,
}: RegisterPageProps) {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [serverEmailError, setServerEmailError] = useState("");

  const [selectedfield_FullName, setselectedfield_FullName] = useState(false);
  const [selectedfield_Email, setselectedfield_Email] = useState(false);
  const [selectedfield_Password, setselectedfield_Password] = useState(false);

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const isFullNameValid = fullName.trim().length > 0;
  const isEmailValid = emailRegex.test(email);
  const isPasswordValid = password.trim().length > 0;
  const isFormValid = isFullNameValid && isEmailValid && isPasswordValid;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setServerEmailError("");

    try {
      const res = await axios.post("/api/reg/", {
        fullName,
        email,
        password,
      });
      const { user } = res.data;
      onRegisterSuccess(user);
    } catch (err: any) {
      const message =
        err?.response?.data?.error || err?.message || "Registration failed.";
      if (message.toLowerCase().includes("exisisting")) {
        setServerEmailError("User with this email already exists.");
        setselectedfield_Email(true);
      }
    }
  };

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
          Register
        </h2>

        <Form noValidate onSubmit={handleSubmit}>
          <Form.Group className="mb-3" controlId="registerFullName">
            <Form.Label>Fullname</Form.Label>
            <Form.Control
              type="text"
              placeholder="John Doe"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              onBlur={() => setselectedfield_FullName(true)}
              isInvalid={selectedfield_FullName && !isFullNameValid}
            />
            {selectedfield_FullName && !isFullNameValid && (
              <div className="invalid-feedback d-block">
                Fullname is required.
              </div>
            )}
          </Form.Group>

          <Form.Group className="mb-3" controlId="registerEmail">
            <Form.Label>Email address</Form.Label>
            <Form.Control
              type="email"
              placeholder="Enter email"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                setServerEmailError("");
              }}
              onBlur={() => setselectedfield_Email(true)}
              isInvalid={
                (selectedfield_Email && !isEmailValid) || !!serverEmailError
              }
            />
            {selectedfield_Email && !isEmailValid && (
              <div className="invalid-feedback d-block">
                Email must be valid.
              </div>
            )}
            {serverEmailError && (
              <div className="invalid-feedback d-block">{serverEmailError}</div>
            )}
          </Form.Group>

          <Form.Group className="mb-3" controlId="registerPassword">
            <Form.Label>Password</Form.Label>
            <Form.Control
              type="password"
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onBlur={() => setselectedfield_Password(true)}
              isInvalid={selectedfield_Password && !isPasswordValid}
            />
            {selectedfield_Password && !isPasswordValid && (
              <div className="invalid-feedback d-block">
                Password is required.
              </div>
            )}
          </Form.Group>

          <div className="d-grid">
            <Button
              type="submit"
              disabled={!isFormValid}
              style={{
                backgroundColor: isFormValid ? "#17479E" : "#6c87b9",
                borderColor: isFormValid ? "#17479E" : "#6c87b9",
                cursor: isFormValid ? "pointer" : "not-allowed",
              }}
            >
              Register
            </Button>
          </div>
        </Form>
      </Container>

      <p className="text-center mt-3" style={{ fontSize: "0.95rem" }}>
        Already have an account?{" "}
        <span
          onClick={onSwitchToLogin}
          style={{
            color: "#0d6efd",
            cursor: "pointer",
            textDecoration: "underline",
          }}
        >
          Login
        </span>
      </p>
    </>
  );
}

export default RegisterPage;
