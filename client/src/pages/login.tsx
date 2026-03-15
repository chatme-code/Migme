import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { Link, useLocation } from "wouter";
import { apiRequest } from "@/lib/queryClient";
import { useToast } from "@/hooks/use-toast";
import { Form, FormControl, FormField, FormItem, FormMessage } from "@/components/ui/form";
import { Eye, EyeOff, ChevronRight } from "lucide-react";
import { loginSchema, type LoginInput } from "@shared/schema";

export default function LoginPage() {
  const { toast } = useToast();
  const [, navigate] = useLocation();
  const [showPassword, setShowPassword] = useState(false);

  const form = useForm<LoginInput>({
    resolver: zodResolver(loginSchema),
    defaultValues: { username: "", password: "" },
  });

  const mutation = useMutation({
    mutationFn: (data: LoginInput) => apiRequest("POST", "/api/auth/login", data),
    onSuccess: async (res) => {
      const body = await res.json();
      toast({ title: "Login Berhasil!", description: `Selamat datang, @${body.user?.username}` });
    },
    onError: async (err: any) => {
      let message = "Username atau password salah.";
      try {
        const body = await err.response?.json?.();
        if (body?.message) message = body.message;
      } catch {}
      toast({ title: "Login Gagal", description: message, variant: "destructive" });
    },
  });

  return (
    <div
      className="min-h-screen flex items-center justify-center p-4"
      style={{ background: "linear-gradient(180deg, #09454A 0%, #0d3a3e 100%)" }}
    >
      <div className="w-full max-w-sm rounded-2xl overflow-hidden shadow-2xl" style={{ boxShadow: "0 20px 60px rgba(0,0,0,0.5)" }}>

        {/* ── HEADER: Star-pattern teal + logo + tabs ── */}
        <div
          className="relative px-6 pt-8 pb-0"
          style={{
            background: "linear-gradient(180deg, #145055 0%, #114c54 100%)",
            backgroundImage: `
              radial-gradient(circle at 15% 25%, rgba(100,185,160,0.15) 0%, transparent 40%),
              radial-gradient(circle at 85% 15%, rgba(100,185,160,0.12) 0%, transparent 35%),
              radial-gradient(circle at 50% 60%, rgba(100,185,160,0.08) 0%, transparent 50%),
              linear-gradient(180deg, #145055 0%, #114c54 100%)
            `,
          }}
        >
          {/* Logo */}
          <div className="text-center mb-6">
            <div
              className="inline-flex items-center justify-center w-16 h-16 rounded-2xl mb-3 shadow-lg"
              style={{ background: "linear-gradient(135deg, #64B9A0 0%, #3a9a7e 100%)" }}
            >
              <span className="text-white font-bold text-2xl tracking-tight">M</span>
            </div>
            <h1 className="text-white font-bold text-2xl tracking-wide">MigxChat</h1>
            <p className="text-xs mt-0.5" style={{ color: "#64B9A0" }}>Fusion Chat Platform</p>
          </div>

          {/* LOGIN / DAFTAR tab bar */}
          <div className="flex border-t border-white/10">
            <div className="flex-1 text-center py-3 relative">
              <span className="text-white font-semibold text-sm tracking-wider">MASUK</span>
              <div
                className="absolute bottom-0 left-1/2 -translate-x-1/2 w-8 h-0.5 rounded-full"
                style={{ background: "#64B9A0" }}
              />
            </div>
            <div className="w-px bg-white/10 my-2" />
            <Link href="/register" className="flex-1 text-center py-3 block" data-testid="link-to-register">
              <span className="font-semibold text-sm tracking-wider" style={{ color: "#64B9A0" }}>DAFTAR</span>
            </Link>
          </div>
        </div>

        {/* ── BANNER: Bot / slogan section ── */}
        <div
          className="flex items-center gap-4 px-6 py-4"
          style={{ background: "#114c54" }}
        >
          <div
            className="flex-shrink-0 w-14 h-14 rounded-full flex items-center justify-center text-2xl shadow-inner"
            style={{ background: "rgba(100,185,160,0.18)", border: "2px solid rgba(100,185,160,0.35)" }}
          >
            🤖
          </div>
          <div>
            <p className="text-white font-semibold text-sm leading-snug">Selamat datang kembali!</p>
            <p className="text-xs mt-0.5" style={{ color: "rgba(100,185,160,0.85)" }}>
              Masuk untuk melanjutkan sesi kamu di MigxChat
            </p>
          </div>
        </div>

        {/* ── FORM: White area ── */}
        <div className="bg-white px-6 pt-6 pb-8">
          <Form {...form}>
            <form onSubmit={form.handleSubmit((d) => mutation.mutate(d))} className="space-y-5">

              {/* Username field — matches Migme SSOResource login: username + password */}
              <FormField
                control={form.control}
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
                          placeholder="Username kamu"
                          autoComplete="username"
                          data-testid="input-username-login"
                          className="w-full mt-1 pb-2 text-sm outline-none bg-transparent border-b-2 transition-colors placeholder:text-gray-300"
                          style={{
                            color: "#424242",
                            borderBottomColor: form.formState.errors.username ? "#C64F44" : "#CCCCCC",
                          }}
                          onFocus={(e) => { e.target.style.borderBottomColor = "#64B9A0"; }}
                          onBlur={(e) => { e.target.style.borderBottomColor = form.formState.errors.username ? "#C64F44" : "#CCCCCC"; }}
                        />
                      </FormControl>
                      <FormMessage className="text-xs mt-1" style={{ color: "#C64F44" }} />
                    </div>
                  </FormItem>
                )}
              />

              {/* Password field */}
              <FormField
                control={form.control}
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
                            placeholder="Password kamu"
                            autoComplete="current-password"
                            data-testid="input-password-login"
                            className="w-full mt-1 pb-2 pr-8 text-sm outline-none bg-transparent border-b-2 transition-colors placeholder:text-gray-300"
                            style={{
                              color: "#424242",
                              borderBottomColor: form.formState.errors.password ? "#C64F44" : "#CCCCCC",
                            }}
                            onFocus={(e) => { e.target.style.borderBottomColor = "#64B9A0"; }}
                            onBlur={(e) => { e.target.style.borderBottomColor = form.formState.errors.password ? "#C64F44" : "#CCCCCC"; }}
                          />
                          <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute right-0 top-1 p-1 text-gray-400 hover:text-gray-600"
                            data-testid="button-toggle-password"
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

              {/* Forgot password */}
              <div className="text-right -mt-2">
                <button type="button" className="text-xs font-medium" style={{ color: "#64B9A0" }}>
                  Lupa password?
                </button>
              </div>

              {/* Submit button */}
              <button
                type="submit"
                disabled={mutation.isPending}
                data-testid="button-login"
                className="w-full py-3 rounded-full font-bold text-white text-sm tracking-wider transition-opacity disabled:opacity-60 flex items-center justify-center gap-2"
                style={{ background: "linear-gradient(90deg, #64B9A0 0%, #3a9a7e 100%)" }}
              >
                {mutation.isPending ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-white/40 border-t-white rounded-full animate-spin" />
                    Masuk...
                  </span>
                ) : (
                  <span className="flex items-center gap-2">
                    MASUK
                    <ChevronRight className="w-4 h-4" />
                  </span>
                )}
              </button>
            </form>
          </Form>

          {/* Peek inside / discover link */}
          <div className="text-center mt-5 pt-5 border-t border-gray-100">
            <p className="text-xs text-gray-400">Belum kenal MigxChat?</p>
            <Link href="/register">
              <button
                type="button"
                data-testid="button-peek-inside"
                className="mt-2 px-8 py-2 rounded-full text-xs font-bold tracking-widest border-2 transition-colors"
                style={{ borderColor: "#64B9A0", color: "#64B9A0" }}
              >
                INTIP KE DALAM
              </button>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
