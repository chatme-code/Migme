import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { Link } from "wouter";
import { z } from "zod";
import { apiRequest } from "@/lib/queryClient";
import { useToast } from "@/hooks/use-toast";
import { Form, FormControl, FormField, FormItem, FormMessage } from "@/components/ui/form";
import { ChevronLeft, ChevronRight, Eye, EyeOff, CheckCircle, Mail } from "lucide-react";

// ── Schemas per step ─────────────────────────────────────────────────────────
// Step 1 username pattern matches Migme UsernameUtils:
//   ^[a-zA-Z]{1}(\.{0,1}[\w-]){5,}$  →  starts with letter, min 6 chars
const step1Schema = z.object({
  username: z
    .string()
    .min(6, "Username minimal 6 karakter")
    .max(30, "Username maksimal 30 karakter")
    .regex(
      /^[a-zA-Z](\.?[\w\-]){5,}$/,
      "Harus diawali huruf, min 6 karakter (huruf/angka/titik/strip diizinkan)"
    ),
  displayName: z
    .string()
    .max(50, "Nama tampilan maksimal 50 karakter")
    .optional(),
});

const step2Schema = z.object({
  email: z.string().email("Format email tidak valid"),
});

const step3Schema = z.object({
  password: z.string().min(6, "Password min 6 karakter"),
  confirmPassword: z.string(),
}).refine((d) => d.password === d.confirmPassword, {
  message: "Password tidak cocok",
  path: ["confirmPassword"],
});

type Step1 = z.infer<typeof step1Schema>;
type Step2 = z.infer<typeof step2Schema>;
type Step3 = z.infer<typeof step3Schema>;

// ── Step metadata — matches Android signup fragments ─────────────────────────
const STEPS = [
  { title: "Pilih Username",    emoji: "👤", slogan: "Nama panggilan kamu di Mig33Reborn",       hint: "Minimal 6 karakter, diawali huruf" },
  { title: "Masukkan Email",    emoji: "✉️",  slogan: "Email untuk verifikasi akun kamu",      hint: "Kami akan kirim link aktivasi ke email ini" },
  { title: "Buat Password",     emoji: "🔒", slogan: "Amankan akun kamu dengan password kuat", hint: "Minimal 6 karakter, kombinasi huruf dan angka" },
];

// ── Field styles helper ───────────────────────────────────────────────────────
function fieldStyle(hasError: boolean) {
  return {
    color: "#424242",
    borderBottomColor: hasError ? "#C64F44" : "#CCCCCC",
  };
}

export default function RegisterPage() {
  const { toast } = useToast();
  const [step, setStep] = useState(1);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [formData, setFormData] = useState({ username: "", email: "", password: "", displayName: "" });

  // Step forms
  const form1 = useForm<Step1>({ resolver: zodResolver(step1Schema), defaultValues: { username: "", displayName: "" } });
  const form2 = useForm<Step2>({ resolver: zodResolver(step2Schema), defaultValues: { email: "" } });
  const form3 = useForm<Step3>({ resolver: zodResolver(step3Schema), defaultValues: { password: "", confirmPassword: "" } });

  const mutation = useMutation({
    mutationFn: async (data: typeof formData) => apiRequest("POST", "/api/auth/register", data),
    onSuccess: async (res) => {
      const body = await res.json();
      toast({ title: "Registrasi Berhasil!", description: body.message });
      setStep(4);
    },
    onError: async (err: any) => {
      let message = "Terjadi kesalahan. Coba lagi.";
      try {
        const body = await err.response?.json?.();
        if (body?.message) message = body.message;
      } catch {}
      toast({ title: "Registrasi Gagal", description: message, variant: "destructive" });
    },
  });

  function goBack() {
    if (step > 1) setStep(step - 1);
  }

  function onStep1(data: Step1) {
    setFormData((p) => ({ ...p, username: data.username, displayName: data.displayName || data.username }));
    setStep(2);
  }

  function onStep2(data: Step2) {
    setFormData((p) => ({ ...p, email: data.email }));
    setStep(3);
  }

  function onStep3(data: Step3) {
    const final = { ...formData, password: data.password };
    setFormData(final);
    mutation.mutate(final);
  }

  const currentStep = step <= 3 ? STEPS[step - 1] : null;

  // ── SUCCESS SCREEN (step 4) ──────────────────────────────────────────────
  if (step === 4) {
    return (
      <div
        className="min-h-screen flex items-center justify-center p-4"
        style={{ background: "linear-gradient(180deg, #09454A 0%, #0d3a3e 100%)" }}
      >
        <div className="w-full max-w-sm rounded-2xl overflow-hidden shadow-2xl">
          {/* Header */}
          <div
            className="px-6 pt-8 pb-4 text-center"
            style={{
              background: `radial-gradient(circle at 20% 30%, rgba(100,185,160,0.18) 0%, transparent 50%),
                           radial-gradient(circle at 80% 70%, rgba(100,185,160,0.12) 0%, transparent 40%),
                           linear-gradient(180deg, #145055 0%, #114c54 100%)`,
            }}
          >
            <div className="w-16 h-16 rounded-2xl flex items-center justify-center mx-auto mb-3 shadow-lg"
              style={{ background: "linear-gradient(135deg, #64B9A0 0%, #3a9a7e 100%)" }}>
              <span className="text-white font-bold text-2xl">M</span>
            </div>
            <h1 className="text-white font-bold text-2xl">Mig33Reborn</h1>
          </div>

          {/* Success body */}
          <div className="bg-white px-6 pt-8 pb-10 text-center">
            <div className="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-4"
              style={{ background: "rgba(100,185,160,0.12)" }}>
              <CheckCircle className="w-10 h-10" style={{ color: "#64B9A0" }} />
            </div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">Cek Email Kamu!</h2>
            <p className="text-sm text-gray-500 mb-5 leading-relaxed">
              Kami sudah mengirim link verifikasi ke email kamu. Klik link tersebut untuk mengaktifkan akun.
            </p>
            <div className="rounded-xl p-4 mb-6 flex items-center gap-3" style={{ background: "#f0faf6" }}>
              <Mail className="w-5 h-5 flex-shrink-0" style={{ color: "#64B9A0" }} />
              <div className="text-left">
                <p className="text-xs text-gray-400">Dikirim dari</p>
                <p className="text-sm font-semibold" style={{ color: "#3a9a7e" }}>noreply@mig33.com</p>
              </div>
            </div>
            <Link href="/login">
              <button
                data-testid="link-go-to-login"
                className="w-full py-3 rounded-full font-bold text-white text-sm tracking-wider"
                style={{ background: "linear-gradient(90deg, #64B9A0 0%, #3a9a7e 100%)" }}
              >
                PERGI KE LOGIN
              </button>
            </Link>
          </div>
        </div>
      </div>
    );
  }

  // ── STEP 1–3 SCREENS ────────────────────────────────────────────────────
  return (
    <div
      className="min-h-screen flex items-center justify-center p-4"
      style={{ background: "linear-gradient(180deg, #09454A 0%, #0d3a3e 100%)" }}
    >
      <div className="w-full max-w-sm rounded-2xl overflow-hidden shadow-2xl">

        {/* ── HEADER ── */}
        <div
          className="relative px-6 pt-6 pb-0"
          style={{
            background: `radial-gradient(circle at 20% 20%, rgba(100,185,160,0.18) 0%, transparent 50%),
                         radial-gradient(circle at 80% 80%, rgba(100,185,160,0.12) 0%, transparent 40%),
                         linear-gradient(180deg, #145055 0%, #114c54 100%)`,
          }}
        >
          {/* Back arrow + title + step counter */}
          <div className="flex items-center justify-between mb-4">
            {step > 1 ? (
              <button
                type="button"
                onClick={goBack}
                data-testid="button-back"
                className="flex items-center gap-1 text-white/70 hover:text-white transition-colors"
              >
                <ChevronLeft className="w-5 h-5" />
                <span className="text-sm">Kembali</span>
              </button>
            ) : (
              <Link href="/login" data-testid="link-to-login">
                <span className="text-sm font-semibold" style={{ color: "#64B9A0" }}>MASUK</span>
              </Link>
            )}
            <div className="flex items-center gap-1.5">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="rounded-full transition-all"
                  style={{
                    width: i === step ? "20px" : "6px",
                    height: "6px",
                    background: i <= step ? "#64B9A0" : "rgba(255,255,255,0.2)",
                  }}
                />
              ))}
            </div>
          </div>

          {/* App name */}
          <div className="text-center pb-4">
            <h1 className="text-white font-bold text-xl tracking-wide">
              {currentStep?.title}
            </h1>
          </div>

          {/* Tab bar */}
          <div className="flex border-t border-white/10">
            <Link href="/login" className="flex-1 text-center py-3 block" data-testid="link-tab-login">
              <span className="font-semibold text-sm tracking-wider text-white/50">MASUK</span>
            </Link>
            <div className="w-px bg-white/10 my-2" />
            <div className="flex-1 text-center py-3 relative">
              <span className="text-white font-semibold text-sm tracking-wider">DAFTAR</span>
              <div
                className="absolute bottom-0 left-1/2 -translate-x-1/2 w-8 h-0.5 rounded-full"
                style={{ background: "#64B9A0" }}
              />
            </div>
          </div>
        </div>

        {/* ── BANNER ── */}
        <div
          className="flex items-center gap-4 px-6 py-4"
          style={{ background: "#114c54" }}
        >
          <div
            className="flex-shrink-0 w-14 h-14 rounded-full flex items-center justify-center text-2xl"
            style={{ background: "rgba(100,185,160,0.18)", border: "2px solid rgba(100,185,160,0.35)" }}
          >
            {currentStep?.emoji}
          </div>
          <div>
            <p className="text-white font-semibold text-sm leading-snug">{currentStep?.slogan}</p>
            <p className="text-xs mt-0.5" style={{ color: "rgba(100,185,160,0.85)" }}>
              {currentStep?.hint}
            </p>
          </div>
        </div>

        {/* ── FORM: White area ── */}
        <div className="bg-white px-6 pt-6 pb-8">

          {/* STEP 1 — Username + Display Name (matches Migme UserData.username + displayName) */}
          {step === 1 && (
            <Form {...form1}>
              <form onSubmit={form1.handleSubmit(onStep1)} className="space-y-5">
                <FormField
                  control={form1.control}
                  name="username"
                  render={({ field }) => (
                    <FormItem>
                      <div>
                        <label className="text-xs font-semibold tracking-widest uppercase" style={{ color: "#64B9A0" }}>
                          Username
                        </label>
                        <FormControl>
                          <input
                            {...field}
                            type="text"
                            placeholder="contoh: johnDoe99"
                            autoComplete="username"
                            data-testid="input-username"
                            className="w-full mt-1 pb-2 text-sm outline-none bg-transparent border-b-2 transition-colors placeholder:text-gray-300"
                            style={fieldStyle(!!form1.formState.errors.username)}
                            onFocus={(e) => { e.target.style.borderBottomColor = "#64B9A0"; }}
                            onBlur={(e) => { e.target.style.borderBottomColor = form1.formState.errors.username ? "#C64F44" : "#CCCCCC"; }}
                          />
                        </FormControl>
                        <FormMessage className="text-xs mt-1" style={{ color: "#C64F44" }} />
                      </div>
                    </FormItem>
                  )}
                />

                <FormField
                  control={form1.control}
                  name="displayName"
                  render={({ field }) => (
                    <FormItem>
                      <div>
                        <label className="text-xs font-semibold tracking-widest uppercase" style={{ color: "#64B9A0" }}>
                          Nama Tampilan <span className="normal-case font-normal text-gray-400">(opsional)</span>
                        </label>
                        <FormControl>
                          <input
                            {...field}
                            type="text"
                            placeholder="Nama yang ditampilkan ke teman"
                            autoComplete="name"
                            data-testid="input-display-name"
                            className="w-full mt-1 pb-2 text-sm outline-none bg-transparent border-b-2 transition-colors placeholder:text-gray-300"
                            style={fieldStyle(!!form1.formState.errors.displayName)}
                            onFocus={(e) => { e.target.style.borderBottomColor = "#64B9A0"; }}
                            onBlur={(e) => { e.target.style.borderBottomColor = form1.formState.errors.displayName ? "#C64F44" : "#CCCCCC"; }}
                          />
                        </FormControl>
                        <FormMessage className="text-xs mt-1" style={{ color: "#C64F44" }} />
                      </div>
                    </FormItem>
                  )}
                />

                <StepButton label="LANJUT" />
              </form>
            </Form>
          )}

          {/* STEP 2 — Email */}
          {step === 2 && (
            <Form {...form2}>
              <form onSubmit={form2.handleSubmit(onStep2)} className="space-y-5">
                <FormField
                  control={form2.control}
                  name="email"
                  render={({ field }) => (
                    <FormItem>
                      <div>
                        <label className="text-xs font-semibold tracking-widest uppercase" style={{ color: "#64B9A0" }}>
                          Email
                        </label>
                        <FormControl>
                          <input
                            {...field}
                            type="email"
                            placeholder="kamu@email.com"
                            autoComplete="email"
                            data-testid="input-email"
                            className="w-full mt-1 pb-2 text-sm outline-none bg-transparent border-b-2 transition-colors placeholder:text-gray-300"
                            style={fieldStyle(!!form2.formState.errors.email)}
                            onFocus={(e) => { e.target.style.borderBottomColor = "#64B9A0"; }}
                            onBlur={(e) => { e.target.style.borderBottomColor = form2.formState.errors.email ? "#C64F44" : "#CCCCCC"; }}
                          />
                        </FormControl>
                        <FormMessage className="text-xs mt-1" style={{ color: "#C64F44" }} />
                      </div>
                    </FormItem>
                  )}
                />

                <div className="text-xs text-gray-400 rounded-lg p-3" style={{ background: "#f0faf6" }}>
                  <span style={{ color: "#64B9A0" }}>✓</span>{" "}
                  Link verifikasi akan dikirim dari{" "}
                  <span className="font-semibold" style={{ color: "#3a9a7e" }}>noreply@migxchat.net</span>
                </div>

                <StepButton label="LANJUT" />
              </form>
            </Form>
          )}

          {/* STEP 3 — Password */}
          {step === 3 && (
            <Form {...form3}>
              <form onSubmit={form3.handleSubmit(onStep3)} className="space-y-5">
                <FormField
                  control={form3.control}
                  name="password"
                  render={({ field }) => (
                    <FormItem>
                      <div>
                        <label className="text-xs font-semibold tracking-widest uppercase" style={{ color: "#64B9A0" }}>
                          Password
                        </label>
                        <FormControl>
                          <div className="relative">
                            <input
                              {...field}
                              type={showPassword ? "text" : "password"}
                              placeholder="Min. 6 karakter"
                              autoComplete="new-password"
                              data-testid="input-password"
                              className="w-full mt-1 pb-2 pr-8 text-sm outline-none bg-transparent border-b-2 transition-colors placeholder:text-gray-300"
                              style={fieldStyle(!!form3.formState.errors.password)}
                              onFocus={(e) => { e.target.style.borderBottomColor = "#64B9A0"; }}
                              onBlur={(e) => { e.target.style.borderBottomColor = form3.formState.errors.password ? "#C64F44" : "#CCCCCC"; }}
                            />
                            <button
                              type="button"
                              onClick={() => setShowPassword(!showPassword)}
                              className="absolute right-0 top-1 p-1 text-gray-400 hover:text-gray-600"
                            >
                              {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                            </button>
                          </div>
                        </FormControl>
                        <FormMessage className="text-xs mt-1" style={{ color: "#C64F44" }} />
                      </div>
                    </FormItem>
                  )}
                />

                <FormField
                  control={form3.control}
                  name="confirmPassword"
                  render={({ field }) => (
                    <FormItem>
                      <div>
                        <label className="text-xs font-semibold tracking-widest uppercase" style={{ color: "#64B9A0" }}>
                          Konfirmasi Password
                        </label>
                        <FormControl>
                          <div className="relative">
                            <input
                              {...field}
                              type={showConfirm ? "text" : "password"}
                              placeholder="Ulangi password"
                              autoComplete="new-password"
                              data-testid="input-confirm-password"
                              className="w-full mt-1 pb-2 pr-8 text-sm outline-none bg-transparent border-b-2 transition-colors placeholder:text-gray-300"
                              style={fieldStyle(!!form3.formState.errors.confirmPassword)}
                              onFocus={(e) => { e.target.style.borderBottomColor = "#64B9A0"; }}
                              onBlur={(e) => { e.target.style.borderBottomColor = form3.formState.errors.confirmPassword ? "#C64F44" : "#CCCCCC"; }}
                            />
                            <button
                              type="button"
                              onClick={() => setShowConfirm(!showConfirm)}
                              className="absolute right-0 top-1 p-1 text-gray-400 hover:text-gray-600"
                            >
                              {showConfirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                            </button>
                          </div>
                        </FormControl>
                        <FormMessage className="text-xs mt-1" style={{ color: "#C64F44" }} />
                      </div>
                    </FormItem>
                  )}
                />

                <StepButton label="DAFTAR SEKARANG" loading={mutation.isPending} />

                <p className="text-center text-xs text-gray-400">
                  Dengan mendaftar, kamu menyetujui{" "}
                  <a href="https://migxchat.net/terms" className="underline" style={{ color: "#64B9A0" }}>
                    Syarat & Ketentuan
                  </a>{" "}
                  dan{" "}
                  <a href="https://migxchat.net/privacy" className="underline" style={{ color: "#64B9A0" }}>
                    Kebijakan Privasi
                  </a>{" "}
                  kami.
                </p>
              </form>
            </Form>
          )}
        </div>
      </div>
    </div>
  );
}

// ── Shared next/submit button ────────────────────────────────────────────────
function StepButton({ label, loading = false }: { label: string; loading?: boolean }) {
  return (
    <button
      type="submit"
      disabled={loading}
      data-testid="button-register-next"
      className="w-full py-3 rounded-full font-bold text-white text-sm tracking-wider transition-opacity disabled:opacity-60 flex items-center justify-center gap-2"
      style={{ background: "linear-gradient(90deg, #64B9A0 0%, #3a9a7e 100%)" }}
    >
      {loading ? (
        <span className="flex items-center gap-2">
          <span className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
          Mendaftar...
        </span>
      ) : (
        <span className="flex items-center gap-2">
          {label}
          <ChevronRight className="w-4 h-4" />
        </span>
      )}
    </button>
  );
}
