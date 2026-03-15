import { useEffect, useState } from "react";
import { Link } from "wouter";
import { CheckCircle, XCircle, Loader2 } from "lucide-react";

type VerifyState = "loading" | "success" | "already" | "error";

export default function VerifyEmailPage() {
  const [state, setState] = useState<VerifyState>("loading");
  const [message, setMessage] = useState("");
  const [username, setUsername] = useState("");

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");

    if (!token) {
      setState("error");
      setMessage("Token verifikasi tidak ditemukan.");
      return;
    }

    fetch(`/api/auth/verify-email?token=${encodeURIComponent(token)}`)
      .then(async (res) => {
        const body = await res.json();
        if (res.ok) {
          setState(body.alreadyVerified ? "already" : "success");
          setUsername(body.username || "");
          setMessage(body.message);
        } else {
          setState("error");
          setMessage(body.message || "Verifikasi gagal.");
        }
      })
      .catch(() => {
        setState("error");
        setMessage("Terjadi kesalahan jaringan. Coba lagi.");
      });
  }, []);

  return (
    <div
      className="min-h-screen flex items-center justify-center p-4"
      style={{ background: "linear-gradient(180deg, #09454A 0%, #0d3a3e 100%)" }}
    >
      <div className="w-full max-w-sm rounded-2xl overflow-hidden shadow-2xl">

        {/* Header */}
        <div
          className="px-6 pt-8 pb-5 text-center"
          style={{
            background: `radial-gradient(circle at 20% 30%, rgba(100,185,160,0.18) 0%, transparent 50%),
                         radial-gradient(circle at 80% 70%, rgba(100,185,160,0.12) 0%, transparent 40%),
                         linear-gradient(180deg, #145055 0%, #114c54 100%)`,
          }}
        >
          <div
            className="inline-flex items-center justify-center w-16 h-16 rounded-2xl mb-3 shadow-lg"
            style={{ background: "linear-gradient(135deg, #64B9A0 0%, #3a9a7e 100%)" }}
          >
            <span className="text-white font-bold text-2xl">M</span>
          </div>
          <h1 className="text-white font-bold text-2xl tracking-wide">MigxChat</h1>
          <p className="text-xs mt-0.5" style={{ color: "#64B9A0" }}>Verifikasi Email</p>
        </div>

        {/* Body */}
        <div className="bg-white px-6 pt-8 pb-10 text-center">

          {/* Loading */}
          {state === "loading" && (
            <>
              <div className="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-5"
                style={{ background: "rgba(100,185,160,0.10)" }}>
                <Loader2 className="w-10 h-10 animate-spin" style={{ color: "#64B9A0" }} />
              </div>
              <h2 className="text-xl font-bold text-gray-800 mb-2">Memverifikasi Akun...</h2>
              <p className="text-sm text-gray-400">Mohon tunggu sebentar.</p>
            </>
          )}

          {/* Success */}
          {state === "success" && (
            <>
              <div className="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-5"
                style={{ background: "rgba(100,185,160,0.12)" }}>
                <CheckCircle className="w-10 h-10" style={{ color: "#64B9A0" }} />
              </div>
              <h2 className="text-xl font-bold text-gray-800 mb-2">Akun Terverifikasi!</h2>
              {username && (
                <p className="font-semibold mb-1 text-sm" style={{ color: "#64B9A0" }}>
                  Selamat datang, @{username}!
                </p>
              )}
              <p className="text-sm text-gray-400 mb-6 leading-relaxed">{message}</p>
              <Link href="/login">
                <button
                  data-testid="button-go-to-login"
                  className="w-full py-3 rounded-full font-bold text-white text-sm tracking-wider"
                  style={{ background: "linear-gradient(90deg, #64B9A0 0%, #3a9a7e 100%)" }}
                >
                  LOGIN SEKARANG
                </button>
              </Link>
            </>
          )}

          {/* Already verified */}
          {state === "already" && (
            <>
              <div className="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-5"
                style={{ background: "rgba(100,185,160,0.10)" }}>
                <CheckCircle className="w-10 h-10" style={{ color: "#3a9a7e" }} />
              </div>
              <h2 className="text-xl font-bold text-gray-800 mb-2">Sudah Terverifikasi</h2>
              <p className="text-sm text-gray-400 mb-6 leading-relaxed">{message}</p>
              <Link href="/login">
                <button
                  data-testid="button-go-to-login-already"
                  className="w-full py-3 rounded-full font-bold text-sm tracking-wider border-2 transition-colors"
                  style={{ borderColor: "#64B9A0", color: "#64B9A0", background: "transparent" }}
                >
                  LOGIN
                </button>
              </Link>
            </>
          )}

          {/* Error */}
          {state === "error" && (
            <>
              <div className="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-5"
                style={{ background: "rgba(198,79,68,0.10)" }}>
                <XCircle className="w-10 h-10" style={{ color: "#C64F44" }} />
              </div>
              <h2 className="text-xl font-bold text-gray-800 mb-2">Verifikasi Gagal</h2>
              <p className="text-sm text-gray-400 mb-6 leading-relaxed">{message}</p>
              <Link href="/register">
                <button
                  data-testid="button-go-to-register"
                  className="w-full py-3 rounded-full font-bold text-sm tracking-wider border-2 transition-colors"
                  style={{ borderColor: "#64B9A0", color: "#64B9A0", background: "transparent" }}
                >
                  DAFTAR ULANG
                </button>
              </Link>
            </>
          )}

        </div>
      </div>
    </div>
  );
}
