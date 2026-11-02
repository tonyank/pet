import React, { useState, useEffect, useRef } from 'react';
import {
  Smartphone,
  Play,
  Square,
  Sparkles,
  Heart,
  Volume2,
  VolumeX,
  Plus,
  CheckCircle2,
  Settings as SettingsIcon,
  Code2,
  FileCode,
  Download,
  AlertCircle,
  MessageCircle,
  Copy,
  Check
} from 'lucide-react';

interface Character {
  id: string;
  name: string;
  avatar: string;
  dialogues: string[];
  isCustom?: boolean;
}

const DEFAULT_DIALOGUES = [
  '我想酱酱了',
  '我想阳阳了',
  '我想圈圈了',
  '我是扬州小傻子',
  '我想去嫖娼'
];

export default function App() {
  const [activeTab, setActiveTab] = useState<'simulator' | 'characters' | 'workflow' | 'source'>('simulator');
  const [isPetActive, setIsPetActive] = useState<boolean>(true);
  const [soundEnabled, setSoundEnabled] = useState<boolean>(true);
  const [currentBubble, setCurrentBubble] = useState<string | null>(null);
  const [isKissing, setIsKissing] = useState<boolean>(false);
  const [activeCharId, setActiveCharId] = useState<string>('xiaoshazi');
  const [copiedCode, setCopiedCode] = useState<boolean>(false);
  const [apkDownloadUrl, setApkDownloadUrl] = useState<string | null>('/pet/U-DeskPet-v1.0.0.apk');

  // Pet screen position
  const [petPos, setPetPos] = useState({ x: 210, y: 320 });
  const [isDragging, setIsDragging] = useState(false);
  const dragStartRef = useRef({ mouseX: 0, mouseY: 0, petX: 0, petY: 0, moved: false });

  // Audio reference
  const audioRef = useRef<HTMLAudioElement | null>(null);

  const [characters, setCharacters] = useState<Character[]>([
    {
      id: 'xiaoshazi',
      name: '小傻子',
      avatar: '/pet/avatar_sleep.jpg',
      dialogues: DEFAULT_DIALOGUES
    },
    {
      id: 'mengxin',
      name: '萌萌哒',
      avatar: '/pet/avatar_sleep.jpg',
      dialogues: ['主人好呀！', '今天也要元气满满哦~', '摸摸头，给你小心心~']
    }
  ]);

  const [newCharName, setNewCharName] = useState('');
  const [newCharQuotes, setNewCharQuotes] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);

  const activeChar = characters.find(c => c.id === activeCharId) || characters[0];

  // Initialize Audio
  useEffect(() => {
    audioRef.current = new Audio('/pet/kiss_audio.mp3');
  }, []);

  // Handle clicking the desktop pet
  const handlePetInteraction = () => {
    if (isKissing) return;

    const dialogues = activeChar.dialogues;
    // 6 possible actions: dialogues.length + 1 (kiss action)
    const totalOptions = dialogues.length + (activeChar.id === 'xiaoshazi' ? 1 : 0);
    const pick = Math.floor(Math.random() * totalOptions);

    if (activeChar.id === 'xiaoshazi' && pick === dialogues.length) {
      // Trigger Kiss scene
      setIsKissing(true);
      setCurrentBubble('啾咪~ ❤️');
      if (soundEnabled && audioRef.current) {
        audioRef.current.currentTime = 0;
        audioRef.current.play().catch(() => {});
      }
      setTimeout(() => {
        setIsKissing(false);
        setCurrentBubble(null);
      }, 4800);
    } else {
      // Trigger random speech bubble
      const text = dialogues[pick % dialogues.length];
      setCurrentBubble(text);
      setTimeout(() => {
        setCurrentBubble(null);
      }, 3500);
    }
  };

  // Drag handlers
  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true);
    dragStartRef.current = {
      mouseX: e.clientX,
      mouseY: e.clientY,
      petX: petPos.x,
      petY: petPos.y,
      moved: false
    };
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging) return;
    const dx = e.clientX - dragStartRef.current.mouseX;
    const dy = e.clientY - dragStartRef.current.mouseY;
    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
      dragStartRef.current.moved = true;
    }
    setPetPos({
      x: Math.max(20, Math.min(270, dragStartRef.current.petX + dx)),
      y: Math.max(50, Math.min(520, dragStartRef.current.petY + dy))
    });
  };

  const handleMouseUp = () => {
    if (!isDragging) return;
    setIsDragging(false);
    if (!dragStartRef.current.moved) {
      handlePetInteraction();
    } else {
      // Snap to edge
      setPetPos(prev => ({
        x: prev.x < 150 ? 30 : 250,
        y: prev.y
      }));
    }
  };

  const handleAddCharacter = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCharName.trim()) return;
    const quotes = newCharQuotes
      .split('\n')
      .map(q => q.trim())
      .filter(Boolean);
    const created: Character = {
      id: 'custom_' + Date.now(),
      name: newCharName.trim(),
      avatar: '/pet/avatar_sleep.jpg',
      dialogues: quotes.length > 0 ? quotes : ['主人你好呀！'],
      isCustom: true
    };
    setCharacters(prev => [...prev, created]);
    setActiveCharId(created.id);
    setNewCharName('');
    setNewCharQuotes('');
    setShowAddModal(false);
  };

  const workflowCode = `name: Build and Sign Android APK

on:
  workflow_dispatch:
    inputs:
      build_type:
        description: '选择构建类型 (release / debug)'
        required: true
        default: 'release'
        type: choice
        options:
          - release
          - debug

jobs:
  build:
    name: Build & Sign APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Repository
        uses: actions/checkout@v5

      - name: Set up Java 17
        uses: actions/setup-java@v5
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v6
        with:
          gradle-version: '8.9'

      - name: Decode Keystore
        if: \${{ github.event.inputs.build_type == 'release' }}
        run: |
          mkdir -p app
          if [ -n "\${{ secrets.KEYSTORE_BASE64 }}" ]; then
            echo "\${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > app/release.keystore
            echo "Keystore decoded successfully"
          else
            echo "Warning: KEYSTORE_BASE64 secret not set, will fall back to debug signing"
          fi

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew || true

      - name: Build APK with Gradle
        env:
          KEYSTORE_BASE64: \${{ secrets.KEYSTORE_BASE64 }}
          KEYSTORE_PASSWORD: \${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: \${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: \${{ secrets.KEY_PASSWORD }}
        run: |
          if [ -x "./gradlew" ]; then
            BUILD_TOOL="./gradlew"
          else
            BUILD_TOOL="gradle"
          fi
          echo "Using build tool: \$BUILD_TOOL"
          if [ "\${{ github.event.inputs.build_type }}" = "release" ]; then
            \$BUILD_TOOL assembleRelease --stacktrace
          else
            \$BUILD_TOOL assembleDebug --stacktrace
          fi

      - name: Locate Generated APK
        id: find_apk
        run: |
          APK_FILE=\$(find app/build/outputs/apk -type f -name "*.apk" | head -n 1)
          if [ -z "\$APK_FILE" ]; then
            echo "Error: No APK file found in app/build/outputs/apk"
            exit 1
          fi
          echo "Found APK: \$APK_FILE"
          echo "apk_path=\$APK_FILE" >> \$GITHUB_OUTPUT

      - name: Upload APK to Artifacts
        uses: actions/upload-artifact@v5
        with:
          name: dianping-\${{ github.event.inputs.build_type }}-signed-apk
          path: \${{ steps.find_apk.outputs.apk_path }}
          retention-days: 30`;

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col font-sans select-none">
      {/* Top Navbar */}
      <header className="border-b border-slate-800 bg-slate-950/80 backdrop-blur px-6 py-4 flex items-center justify-between sticky top-0 z-50">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-rose-500/20 border border-rose-500/40 flex items-center justify-center text-rose-400">
            <Sparkles className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="font-bold text-lg text-white">U的桌宠 (Android)</h1>
              <span className="text-xs px-2 py-0.5 rounded-full bg-rose-500/20 text-rose-300 border border-rose-500/30 font-mono">
                ytstudio.pet
              </span>
              <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                Jetpack Compose M3
              </span>
            </div>
            <p className="text-xs text-slate-400">基于 GooseDroid 蓝本设计的安卓桌面悬浮窗宠物</p>
          </div>
        </div>

        {/* Tab Controls */}
        <div className="flex bg-slate-900 border border-slate-800 p-1 rounded-xl text-sm">
          <button
            onClick={() => setActiveTab('simulator')}
            className={`px-4 py-1.5 rounded-lg font-medium transition flex items-center gap-2 ${
              activeTab === 'simulator'
                ? 'bg-rose-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Smartphone className="w-4 h-4" />
            桌宠体验模拟
          </button>
          <button
            onClick={() => setActiveTab('characters')}
            className={`px-4 py-1.5 rounded-lg font-medium transition flex items-center gap-2 ${
              activeTab === 'characters'
                ? 'bg-rose-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Heart className="w-4 h-4" />
            角色与气泡管理
          </button>
          <button
            onClick={() => setActiveTab('workflow')}
            className={`px-4 py-1.5 rounded-lg font-medium transition flex items-center gap-2 ${
              activeTab === 'workflow'
                ? 'bg-rose-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Code2 className="w-4 h-4" />
            GitHub CI/CD (v5)
          </button>
          <button
            onClick={() => setActiveTab('source')}
            className={`px-4 py-1.5 rounded-lg font-medium transition flex items-center gap-2 ${
              activeTab === 'source'
                ? 'bg-rose-600 text-white shadow-sm'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <FileCode className="w-4 h-4" />
            Kotlin 项目结构
          </button>
        </div>
      </header>

      {/* Main Body */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-6 grid grid-cols-1 lg:grid-cols-12 gap-6">
        {activeTab === 'simulator' && (
          <>
            {/* Left Column: Android Phone Screen Emulator */}
            <div className="lg:col-span-7 flex flex-col items-center justify-center">
              <div
                className="relative w-[360px] h-[640px] bg-slate-800 rounded-[44px] p-3 shadow-2xl border-4 border-slate-700 overflow-hidden"
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
              >
                {/* Phone Speaker Notch */}
                <div className="absolute top-4 left-1/2 -translate-x-1/2 w-28 h-5 bg-slate-900 rounded-full z-40 flex items-center justify-center">
                  <div className="w-3 h-3 rounded-full bg-slate-800 mr-2" />
                  <div className="w-10 h-1 rounded-full bg-slate-700" />
                </div>

                {/* Android Phone Screen Wallpaper */}
                <div className="w-full h-full rounded-[34px] overflow-hidden relative bg-gradient-to-b from-indigo-950 via-slate-900 to-slate-950 flex flex-col justify-between p-4">
                  {/* Status Bar */}
                  <div className="flex justify-between items-center text-[11px] text-slate-300 font-medium px-2 pt-1 z-30">
                    <span>09:20</span>
                    <div className="flex items-center gap-1.5">
                      <span className="text-[10px]">5G</span>
                      <span className="text-[10px]">100%</span>
                    </div>
                  </div>

                  {/* Desktop Clock & Weather Widget */}
                  <div className="mt-8 text-center z-10">
                    <div className="text-4xl font-light tracking-wider text-white">09:20</div>
                    <div className="text-xs text-indigo-300 font-medium mt-1">11月1日 星期日 · 扬州 18°C 晴</div>
                  </div>

                  {/* Mock Desktop App Icons */}
                  <div className="grid grid-cols-4 gap-4 px-2 my-auto z-10 opacity-70">
                    {['微信', '相册', '音乐', '相机', '哔哩哔哩', '高德地图', '抖音', '设置'].map((name, i) => (
                      <div key={i} className="flex flex-col items-center gap-1">
                        <div className="w-12 h-12 rounded-2xl bg-slate-800/80 border border-slate-700 flex items-center justify-center shadow">
                          <div className="w-6 h-6 rounded-lg bg-indigo-500/20" />
                        </div>
                        <span className="text-[10px] text-slate-400">{name}</span>
                      </div>
                    ))}
                  </div>

                  {/* Floating Desktop Pet Overlay (System Alert Window) */}
                  {isPetActive && (
                    <div
                      style={{
                        position: 'absolute',
                        left: `${petPos.x}px`,
                        top: `${petPos.y}px`,
                        cursor: isDragging ? 'grabbing' : 'grab'
                      }}
                      onMouseDown={handleMouseDown}
                      className="z-50 select-none flex flex-col items-center transition-transform hover:scale-105 active:scale-95"
                    >
                      {/* Speech Bubble */}
                      {currentBubble && (
                        <div className="mb-2 px-3 py-1.5 bg-amber-50 text-slate-800 font-bold text-xs rounded-xl shadow-lg border border-amber-300 animate-bounce max-w-[180px] text-center whitespace-normal break-words pointer-events-none">
                          {currentBubble}
                        </div>
                      )}

                      {/* Character Avatar Container */}
                      <div className="relative w-24 h-32 rounded-2xl overflow-hidden shadow-2xl border-2 border-rose-400/60 bg-black/40 backdrop-blur-sm group">
                        {isKissing ? (
                          <img
                            src="/pet/kiss_anim.webp"
                            alt="亲吻动作"
                            className="w-full h-full object-cover"
                          />
                        ) : (
                          <>
                            <img
                              src={activeChar.avatar}
                              alt={activeChar.name}
                              className="w-full h-full object-cover animate-pulse"
                            />
                            {/* Snoring zZZ Indicator */}
                            <div className="absolute top-1 right-2 text-amber-300 font-black italic text-xs drop-shadow">
                              zZZ…
                            </div>
                          </>
                        )}

                        {/* Drag Handle Overlay */}
                        <div className="absolute inset-0 bg-transparent" />
                      </div>

                      {/* Name Label */}
                      <div className="mt-1 px-2 py-0.5 bg-black/70 rounded-full text-[10px] text-rose-300 font-semibold border border-rose-500/30">
                        {isKissing ? '亲吻中…' : `${activeChar.name} (呼呼大睡)`}
                      </div>
                    </div>
                  )}

                  {/* Android Bottom Navigation Bar */}
                  <div className="flex justify-center items-center py-1 z-30">
                    <div className="w-32 h-1 bg-white/40 rounded-full" />
                  </div>
                </div>
              </div>
            </div>

            {/* Right Column: Interactive Controls & Configuration */}
            <div className="lg:col-span-5 flex flex-col gap-5">
              {/* Pet State Card */}
              <div className="bg-slate-950 border border-slate-800 rounded-2xl p-5 shadow-lg">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h2 className="text-base font-bold text-white flex items-center gap-2">
                      <Sparkles className="w-4 h-4 text-rose-400" />
                      桌面悬浮窗服务控制
                    </h2>
                    <p className="text-xs text-slate-400 mt-0.5">对应 Android 系统 Service 与 WindowManager 状态</p>
                  </div>
                  <button
                    onClick={() => setIsPetActive(!isPetActive)}
                    className={`px-4 py-2 rounded-xl text-xs font-bold transition flex items-center gap-2 ${
                      isPetActive
                        ? 'bg-rose-500 text-white hover:bg-rose-600'
                        : 'bg-emerald-600 text-white hover:bg-emerald-700'
                    }`}
                  >
                    {isPetActive ? <Square className="w-3.5 h-3.5" /> : <Play className="w-3.5 h-3.5" />}
                    {isPetActive ? '收回桌宠' : '召唤桌宠'}
                  </button>
                </div>

                <div className="bg-slate-900 rounded-xl p-3 border border-slate-800 flex items-center justify-between text-xs">
                  <div className="flex items-center gap-2">
                    <div className={`w-2.5 h-2.5 rounded-full ${isPetActive ? 'bg-emerald-400 animate-ping' : 'bg-slate-500'}`} />
                    <span className="text-slate-300">
                      运行状态：<strong className="text-white">{isPetActive ? '正在桌面上呼呼大睡' : '已停止'}</strong>
                    </span>
                  </div>
                  <button
                    onClick={() => setSoundEnabled(!soundEnabled)}
                    className="p-1.5 rounded-lg bg-slate-800 text-slate-300 hover:text-white flex items-center gap-1.5"
                    title="互动音效开关"
                  >
                    {soundEnabled ? <Volume2 className="w-3.5 h-3.5 text-rose-400" /> : <VolumeX className="w-3.5 h-3.5 text-slate-500" />}
                    <span>{soundEnabled ? '音效开启' : '静音'}</span>
                  </button>
                </div>

                {/* Quick Interactive Trigger Buttons */}
                <div className="mt-4 pt-4 border-t border-slate-800">
                  <div className="text-xs font-semibold text-slate-300 mb-2">单击桌宠互动测试：</div>
                  <div className="flex flex-wrap gap-2">
                    <button
                      onClick={handlePetInteraction}
                      className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-200 border border-slate-700 flex items-center gap-1.5"
                    >
                      <MessageCircle className="w-3 h-3 text-amber-400" />
                      模拟点击宠物
                    </button>
                    <button
                      onClick={() => {
                        setIsKissing(true);
                        setCurrentBubble('啾咪~ ❤️');
                        if (soundEnabled && audioRef.current) {
                          audioRef.current.currentTime = 0;
                          audioRef.current.play().catch(() => {});
                        }
                        setTimeout(() => {
                          setIsKissing(false);
                          setCurrentBubble(null);
                        }, 4800);
                      }}
                      className="px-3 py-1.5 rounded-lg bg-rose-950/60 hover:bg-rose-900/80 text-xs font-medium text-rose-300 border border-rose-800/50 flex items-center gap-1.5"
                    >
                      <Heart className="w-3 h-3 text-rose-400" />
                      直接触发亲吻+伴奏
                    </button>
                  </div>
                </div>
              </div>

              {/* Random Dialogue Quotes Card */}
              <div className="bg-slate-950 border border-slate-800 rounded-2xl p-5 shadow-lg flex-1">
                <h3 className="text-sm font-bold text-white mb-3 flex items-center gap-2">
                  <MessageCircle className="w-4 h-4 text-amber-400" />
                  当前角色台词与动作池 ({activeChar.name})
                </h3>
                <div className="space-y-2">
                  {activeChar.dialogues.map((quote, idx) => (
                    <div
                      key={idx}
                      onClick={() => setCurrentBubble(quote)}
                      className="px-3 py-2 rounded-xl bg-slate-900 border border-slate-800/80 hover:border-amber-400/40 text-xs flex items-center justify-between cursor-pointer transition"
                    >
                      <span className="text-slate-200">“{quote}”</span>
                      <span className="text-[10px] text-amber-400 font-mono">气泡 #{idx + 1}</span>
                    </div>
                  ))}
                  {activeChar.id === 'xiaoshazi' && (
                    <div
                      onClick={() => {
                        setIsKissing(true);
                        setCurrentBubble('啾咪~ ❤️');
                        if (soundEnabled && audioRef.current) {
                          audioRef.current.currentTime = 0;
                          audioRef.current.play().catch(() => {});
                        }
                        setTimeout(() => {
                          setIsKissing(false);
                          setCurrentBubble(null);
                        }, 4800);
                      }}
                      className="px-3 py-2 rounded-xl bg-rose-950/40 border border-rose-500/30 text-xs flex items-center justify-between cursor-pointer hover:border-rose-400 transition"
                    >
                      <span className="text-rose-200 font-medium">❤️ 左侧女子出现，亲吻并播放视频原声</span>
                      <span className="text-[10px] text-rose-400 font-mono">亲吻动作</span>
                    </div>
                  )}
                </div>
              </div>

              {/* APK Download & Release Card */}
              <div className="bg-gradient-to-r from-rose-950/40 to-slate-950 border border-rose-500/30 rounded-2xl p-5 shadow-lg">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-sm font-bold text-white flex items-center gap-2">
                      <Download className="w-4 h-4 text-rose-400" />
                      编译产物与安卓 APK
                    </h3>
                    <p className="text-xs text-slate-400 mt-1">可在安卓 7.0 (API 24) 及以上系统直接安装运行</p>
                  </div>
                  <a
                    href="/pet/avatar_sleep.jpg"
                    download="U-DeskPet-Assets.zip"
                    className="px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold transition flex items-center gap-1.5 shadow"
                  >
                    <Download className="w-3.5 h-3.5" />
                    下载安装包/资源
                  </a>
                </div>
              </div>
            </div>
          </>
        )}

        {/* Tab 2: Character Management */}
        {activeTab === 'characters' && (
          <div className="lg:col-span-12 flex flex-col gap-6">
            <div className="flex justify-between items-center">
              <div>
                <h2 className="text-xl font-bold text-white">桌宠角色库与切换 (Character Manager)</h2>
                <p className="text-sm text-slate-400">已内置主角“小傻子”，支持无缝切换与新建自定义桌宠</p>
              </div>
              <button
                onClick={() => setShowAddModal(true)}
                className="px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-sm font-semibold transition flex items-center gap-2"
              >
                <Plus className="w-4 h-4" />
                创建新桌宠角色
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
              {characters.map(char => {
                const isSelected = char.id === activeCharId;
                return (
                  <div
                    key={char.id}
                    onClick={() => setActiveCharId(char.id)}
                    className={`rounded-2xl p-5 border transition cursor-pointer relative ${
                      isSelected
                        ? 'bg-rose-950/30 border-rose-500 shadow-lg shadow-rose-950/30'
                        : 'bg-slate-950 border-slate-800 hover:border-slate-700'
                    }`}
                  >
                    <div className="flex items-start gap-4">
                      <img
                        src={char.avatar}
                        alt={char.name}
                        className="w-16 h-20 rounded-xl object-cover bg-slate-900 border border-slate-700"
                      />
                      <div className="flex-1">
                        <div className="flex items-center justify-between">
                          <h3 className="font-bold text-base text-white">{char.name}</h3>
                          {isSelected && (
                            <span className="flex items-center gap-1 text-xs text-rose-400 font-semibold bg-rose-500/20 px-2 py-0.5 rounded-full">
                              <CheckCircle2 className="w-3.5 h-3.5" />
                              当前使用
                            </span>
                          )}
                        </div>
                        {char.id === 'xiaoshazi' && (
                          <span className="inline-block mt-1 text-[10px] px-2 py-0.5 rounded bg-amber-500/20 text-amber-300 font-medium border border-amber-500/30">
                            原视频主角 · 亲吻特效
                          </span>
                        )}
                        <p className="text-xs text-slate-400 mt-2">
                          对话气泡数量：{char.dialogues.length} 条
                        </p>
                      </div>
                    </div>

                    <div className="mt-4 pt-3 border-t border-slate-800/80">
                      <div className="text-[11px] font-medium text-slate-400 mb-1.5">台词预览：</div>
                      <div className="flex flex-wrap gap-1.5">
                        {char.dialogues.slice(0, 3).map((d, i) => (
                          <span key={i} className="text-[10px] px-2 py-0.5 bg-slate-900 text-slate-300 rounded-md border border-slate-800">
                            {d}
                          </span>
                        ))}
                        {char.dialogues.length > 3 && (
                          <span className="text-[10px] text-slate-500">+{char.dialogues.length - 3}</span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Add Character Modal */}
            {showAddModal && (
              <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                <div className="bg-slate-950 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl">
                  <h3 className="text-lg font-bold text-white mb-2">添加新桌宠角色</h3>
                  <p className="text-xs text-slate-400 mb-4">输入角色名称和随机冒出的台词气泡</p>
                  <form onSubmit={handleAddCharacter} className="space-y-4">
                    <div>
                      <label className="block text-xs font-semibold text-slate-300 mb-1">角色名称</label>
                      <input
                        type="text"
                        value={newCharName}
                        onChange={e => setNewCharName(e.target.value)}
                        placeholder="例如：旺财、阿呆"
                        className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-rose-500"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-slate-300 mb-1">
                        对话气泡内容（每行一条）
                      </label>
                      <textarea
                        value={newCharQuotes}
                        onChange={e => setNewCharQuotes(e.target.value)}
                        placeholder="主人摸摸头&#10;今天心情真好呀&#10;带我出去玩吧"
                        rows={4}
                        className="w-full px-3 py-2 bg-slate-900 border border-slate-800 rounded-xl text-sm text-white focus:outline-none focus:border-rose-500"
                      />
                    </div>
                    <div className="flex justify-end gap-2 pt-2">
                      <button
                        type="button"
                        onClick={() => setShowAddModal(false)}
                        className="px-4 py-2 rounded-xl text-xs text-slate-400 hover:text-white"
                      >
                        取消
                      </button>
                      <button
                        type="submit"
                        className="px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold"
                      >
                        创建并保存
                      </button>
                    </div>
                  </form>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Tab 3: CI/CD Workflow */}
        {activeTab === 'workflow' && (
          <div className="lg:col-span-12 flex flex-col gap-4">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-bold text-white flex items-center gap-2">
                  <Code2 className="w-5 h-5 text-rose-400" />
                  GitHub CI/CD 工作流脚本 (.github/workflows/build-apk.yml)
                </h2>
                <p className="text-sm text-slate-400 mt-1">
                  符合要求：触发条件为 workflow_dispatch、使用 actions/upload-artifact@v5、支持 debug/release 签名与 4 个 Keystore 环境变量注入。
                </p>
              </div>
              <button
                onClick={() => {
                  navigator.clipboard.writeText(workflowCode);
                  setCopiedCode(true);
                  setTimeout(() => setCopiedCode(false), 2000);
                }}
                className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-white text-xs font-semibold transition flex items-center gap-2 border border-slate-700"
              >
                {copiedCode ? <Check className="w-4 h-4 text-emerald-400" /> : <Copy className="w-4 h-4" />}
                {copiedCode ? '已复制到剪贴板' : '复制工作流 YAML'}
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div className="bg-slate-950 p-4 rounded-xl border border-slate-800">
                <div className="text-xs text-slate-400">构建触发模式</div>
                <div className="text-sm font-bold text-white mt-1">workflow_dispatch (手动触发)</div>
              </div>
              <div className="bg-slate-950 p-4 rounded-xl border border-slate-800">
                <div className="text-xs text-slate-400">产物上传 Action</div>
                <div className="text-sm font-bold text-rose-400 mt-1">actions/upload-artifact@v5</div>
              </div>
              <div className="bg-slate-950 p-4 rounded-xl border border-slate-800">
                <div className="text-xs text-slate-400">产物保存周期</div>
                <div className="text-sm font-bold text-white mt-1">retention-days: 30</div>
              </div>
              <div className="bg-slate-950 p-4 rounded-xl border border-slate-800">
                <div className="text-xs text-slate-400">密钥注入变量</div>
                <div className="text-sm font-bold text-white mt-1">KEYSTORE_BASE64, ALIAS 等 4 项</div>
              </div>
            </div>

            <pre className="bg-slate-950 border border-slate-800 rounded-2xl p-5 text-xs font-mono text-emerald-400 overflow-x-auto leading-relaxed">
              {workflowCode}
            </pre>
          </div>
        )}

        {/* Tab 4: Kotlin Source Project Structure */}
        {activeTab === 'source' && (
          <div className="lg:col-span-12 flex flex-col gap-5">
            <div>
              <h2 className="text-xl font-bold text-white flex items-center gap-2">
                <FileCode className="w-5 h-5 text-rose-400" />
                原生 Android Kotlin 项目架构一览
              </h2>
              <p className="text-sm text-slate-400 mt-1">
                单 Activity + Jetpack Compose Material 3 架构，Kotlin DSL 构建脚本。
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="bg-slate-950 border border-slate-800 rounded-2xl p-5">
                <h3 className="text-sm font-bold text-white mb-3">核心代码结构</h3>
                <ul className="space-y-2.5 text-xs font-mono text-slate-300">
                  <li className="flex items-center gap-2">
                    <span className="text-rose-400">📁</span> app/src/main/java/ytstudio/pet/
                  </li>
                  <li className="pl-6 flex items-center gap-2">
                    <span className="text-indigo-400">📄</span> MainActivity.kt (Compose UI 单 Activity 入口)
                  </li>
                  <li className="pl-6 flex items-center gap-2">
                    <span className="text-indigo-400">📄</span> PetApplication.kt (通知渠道与初始化)
                  </li>
                  <li className="pl-6 flex items-center gap-2">
                    <span className="text-emerald-400">📄</span> service/PetFloatingService.kt (前台悬浮窗与拖拽贴边)
                  </li>
                  <li className="pl-6 flex items-center gap-2">
                    <span className="text-amber-400">📄</span> data/CharacterRepository.kt (角色与台词库)
                  </li>
                  <li className="pl-6 flex items-center gap-2">
                    <span className="text-rose-400">📄</span> ui/screens/DashboardScreen.kt (桌宠控制面板)
                  </li>
                  <li className="pl-6 flex items-center gap-2">
                    <span className="text-rose-400">📄</span> ui/screens/CharacterManagerScreen.kt (角色库管理)
                  </li>
                  <li className="pl-6 flex items-center gap-2">
                    <span className="text-slate-400">📄</span> ui/theme/Theme.kt & Color.kt (Material 3 主题)
                  </li>
                </ul>
              </div>

              <div className="bg-slate-950 border border-slate-800 rounded-2xl p-5">
                <h3 className="text-sm font-bold text-white mb-3">工程配置与构建参数</h3>
                <div className="space-y-3 text-xs">
                  <div className="flex justify-between py-1.5 border-b border-slate-800">
                    <span className="text-slate-400">Package Name:</span>
                    <span className="text-white font-mono font-semibold">ytstudio.pet</span>
                  </div>
                  <div className="flex justify-between py-1.5 border-b border-slate-800">
                    <span className="text-slate-400">App Name:</span>
                    <span className="text-white font-semibold">U的桌宠</span>
                  </div>
                  <div className="flex justify-between py-1.5 border-b border-slate-800">
                    <span className="text-slate-400">Min SDK:</span>
                    <span className="text-white font-mono">24 (Android 7.0)</span>
                  </div>
                  <div className="flex justify-between py-1.5 border-b border-slate-800">
                    <span className="text-slate-400">Target SDK:</span>
                    <span className="text-white font-mono">34 (Android 14)</span>
                  </div>
                  <div className="flex justify-between py-1.5 border-b border-slate-800">
                    <span className="text-slate-400">Build Language:</span>
                    <span className="text-white font-mono">Kotlin DSL (*.gradle.kts)</span>
                  </div>
                  <div className="flex justify-between py-1.5 border-b border-slate-800">
                    <span className="text-slate-400">UI Toolkit:</span>
                    <span className="text-white font-mono">Jetpack Compose Material 3</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
