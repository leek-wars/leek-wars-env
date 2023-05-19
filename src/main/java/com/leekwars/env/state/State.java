package com.leekwars.env.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.leekwars.env.action.Action;
import com.leekwars.env.action.ActionChestOpened;
import com.leekwars.env.action.ActionEndTurn;
import com.leekwars.env.action.ActionEntityDie;
import com.leekwars.env.action.ActionEntityTurn;
import com.leekwars.env.action.ActionInvocation;
import com.leekwars.env.action.ActionMove;
import com.leekwars.env.action.ActionNewTurn;
import com.leekwars.env.action.ActionResurrect;
import com.leekwars.env.action.ActionSetWeapon;
import com.leekwars.env.action.ActionStartFight;
import com.leekwars.env.action.ActionUseChip;
import com.leekwars.env.action.ActionUseWeapon;
import com.leekwars.env.action.Actions;
import com.leekwars.env.attack.Attack;
import com.leekwars.env.chips.Chip;
import com.leekwars.env.chips.Chips;
import com.leekwars.env.effect.Effect;
import com.leekwars.env.effect.EffectParameters;
import com.leekwars.env.entity.Bulb;
import com.leekwars.env.leek.Leek;
import com.leekwars.env.leek.RegisterManager;
import com.leekwars.env.maps.Cell;
import com.leekwars.env.maps.Map;
import com.leekwars.env.maps.Pathfinding;
import com.leekwars.env.statistics.StatisticsManager;
import com.leekwars.env.util.RandomGenerator;
import com.leekwars.env.weapons.Weapon;

public class State {

	public final static String TAG = State.class.getSimpleName();

	// Fight full types (type + context)
	public static final int TYPE_SOLO_GARDEN = 1;
	public static final int TYPE_SOLO_TEST = 2;
	public static final int TYPE_NORMAL_WHAT = 3;
	public static final int TYPE_TEAM_GARDEN = 4;
	public static final int TYPE_SOLO_CHALLENGE = 5;
	public static final int TYPE_FARMER_GARDEN = 6;
	public static final int TYPE_SOLO_TOURNAMENT = 7;
	public static final int TYPE_TEAM_TEST = 8;
	public static final int TYPE_FARMER_TOURNAMENT = 9;
	public static final int TYPE_TEAM_TOURNAMENT = 10;
	public static final int TYPE_FARMER_CHALLENGE = 11;
	public static final int TYPE_FARMER_TEST = 12;
	public static final int FULL_TYPE_BATTLE_ROYALE = 15;

	// Fight contexts
	public final static int CONTEXT_TEST = 0;
	public final static int CONTEXT_CHALLENGE = 1;
	public final static int CONTEXT_GARDEN = 2;
	public final static int CONTEXT_TOURNAMENT = 3;
	public final static int CONTEXT_BATTLE_ROYALE = 5;

	// Fight types
	public final static int TYPE_SOLO = 0;
	public final static int TYPE_FARMER = 1;
	public final static int TYPE_TEAM = 2;
	public final static int TYPE_BATTLE_ROYALE = 3;

	// Summon limit
	public final static int SUMMON_LIMIT = 8;

	public final static int MAX_TURNS = 64;

	// Fight states
	public final static int STATE_INIT = 0;
	public final static int STATE_RUNNING = 1;
	public final static int STATE_FINISHED = 2;

	private RandomGenerator randomGenerator = new RandomGenerator() {
		private long n = 0;

		public void seed(long seed) {
			n = seed;
		}

		@Override
		public double getDouble() {
			n = n * 1103515245 + 12345;
			long r = (n / 65536) % 32768 + 32768;
			return (double) r / 65536;
		}

		@Override
		public int getInt(int min, int max) {
			if (max - min + 1 <= 0)
				return 0;
			return min + (int) (getDouble() * (max - min + 1));
		}

		@Override
		public long getLong(long min, long max) {
			if (max - min + 1 <= 0)
				return 0;
			return min + (long) (getDouble() * (max - min + 1));
		}
	};

	private final List<Team> teams;
	private final List<Entity> initialOrder;
	private int mNextEntityId = 0;
	private int mWinteam = -1;
	private final java.util.Map<Integer, Entity> mEntities;
	private int mId;
	public int mState = STATE_INIT;
	private Order order;
	private final int fullType;
	private int mStartFarmer = -1;
	private int lastTurn = 0;
	private Date date;
	private Map map;
	private final Actions actions;
	private String mLeekDatas = "";
	private int context;
	private int type;
	public JSONObject custom_map = null;
	public StatisticsManager statistics;
	private RegisterManager registerManager;
	public long executionTime = 0;
	private int seed = 0;

	private int bestItem;

	public State() {

		teams = new ArrayList<Team>();
		initialOrder = new ArrayList<Entity>();

		actions = new Actions();

		mEntities = new HashMap<Integer, Entity>();

		type = TYPE_SOLO;
		context = CONTEXT_GARDEN;
		fullType = TYPE_SOLO_GARDEN;

		date = new Date();
	}

	public State(State state) {
		this.mId = state.mId;
		this.mState = state.mState;
		this.actions = new Actions(state.actions);
		this.randomGenerator = state.randomGenerator;

		mEntities = new HashMap<Integer, Entity>();
		for (var entity : state.mEntities.entrySet()) {
			var newEntity = new Leek((Leek) entity.getValue());
			newEntity.setState(this, entity.getValue().getFId());
			mEntities.put(entity.getKey(), newEntity);
		}

		// Effets
		for (var entity : mEntities.values()) {
			for (var effet : state.mEntities.get(entity.getFId()).effects) {
				var newEffect = (Effect) effet.clone();
				var caster = mEntities.get(newEffect.getCaster().getFId());
				newEffect.setTarget(entity);
				newEffect.setCaster(caster);
				entity.addEffect(newEffect);
				caster.addLaunchedEffect(newEffect);
			}
		}

		this.initialOrder = new ArrayList<Entity>();
		for (var entity : state.initialOrder) {
			this.initialOrder.add(mEntities.get(entity.getFId()));
		}
		this.order = new Order(state.order, this);
		this.teams = new ArrayList<>();
		for (var team : state.teams) {
			this.teams.add(new Team(team, this));
		}

		this.map = new Map(state.map, this);

		this.statistics = state.statistics;
		this.registerManager = state.registerManager;
		this.fullType = state.fullType;
		this.type = state.type;
		this.context = state.context;
		this.mNextEntityId = state.mNextEntityId;
		this.mWinteam = state.mWinteam;
		this.mStartFarmer = state.mStartFarmer;
		this.lastTurn = state.lastTurn;
		this.date = state.date;
		this.mLeekDatas = state.mLeekDatas;
		this.executionTime = state.executionTime;
		this.seed = state.seed;
	}

	public void addFlag(int team, int flag) {
		teams.get(team).addFlag(flag);
	}

	public HashSet<Integer> getFlags(int team) {
		return teams.get(team).getFlags();
	}

	public int getWinner() {
		return mWinteam;
	}

	public void setTeamID(int team, int id) {
		if (team < teams.size()) {
			teams.get(team).setID(id);
		}
	}

	public java.util.Map<Integer, Entity> getEntities() {
		return mEntities;
	}

	public void setStartFarmer(int startFarmer) {
		mStartFarmer = startFarmer;
	}

	public int getStartFarmer() {
		return mStartFarmer;
	}

	public int getTeamID(int team) {
		if (team < teams.size()) {
			return teams.get(team).getID();
		}
		return -1;
	}

	public int getId() {
		return mId;
	}

	public String getLeekDatas() {
		return mLeekDatas;
	}

	public void log(Action log) {
		actions.log(log);
	}

	public Map getMap() {
		return map;
	}

	public void setCustomMap(JSONObject map) {
		custom_map = map;
	}

	/*
	 * Add Entity from json fight 'team' is whether the entity is in 'leeks1' or
	 * 'leeks2'
	 */
	public void addEntity(int t, Entity entity) {

		if (entity == null || t < 0) {
			return;
		}

		int team = t;
		if (type == State.TYPE_BATTLE_ROYALE) {
			team = teams.size();
		}

		while (teams.size() < team + 1) {
			teams.add(new Team());
		}

		entity.setTeam(team);

		teams.get(team).addEntity(entity);

		entity.setState(this, getNextEntityId());
		mEntities.put(entity.getFId(), entity);
	}

	public List<Entity> getEnemiesEntities(int team) {
		return getEnemiesEntities(team, false);
	}

	public List<Entity> getEnemiesEntities(int team, boolean get_deads) {

		List<Entity> enemies = new ArrayList<Entity>();
		for (int t = 0; t < teams.size(); ++t) {
			if (t != team) {
				enemies.addAll(getTeamEntities(t, get_deads));
			}
		}
		return enemies;
	}

	public List<Entity> getTeamLeeks(int team) {
		List<Entity> leeks = new ArrayList<Entity>();
		if (team < teams.size()) {
			for (Entity e : teams.get(team).getEntities()) {
				if (!e.isDead() && e.getType() == Entity.TYPE_LEEK)
					leeks.add(e);
			}
		}
		return leeks;
	}

	public List<Entity> getTeamEntities(int team) {
		return getTeamEntities(team, false);
	}

	public List<Entity> getTeamEntities(int team, boolean dead) {

		List<Entity> leeks = new ArrayList<Entity>();

		if (team < teams.size()) {
			for (Entity e : teams.get(team).getEntities()) {
				if (dead || !e.isDead())
					leeks.add(e);
			}
		}
		return leeks;
	}

	public List<Entity> getAllEntities(boolean get_deads) {

		List<Entity> leeks = new ArrayList<Entity>();

		for (Team t : teams) {
			for (Entity e : t.getEntities()) {
				if (get_deads || !e.isDead()) {
					leeks.add(e);
				}
			}
		}
		return leeks;
	}

	public Entity getEntity(int id) {
		return mEntities.get(id);
	}

	public Entity getEntity(long id) {
		return mEntities.get((int) id);
	}

	public void computeWinner(boolean drawCheckLife) {
		mWinteam = -1;
		int alive = 0;
		for (int t = 0; t < teams.size(); ++t) {
			if (!teams.get(t).isDead() && !teams.get(t).containsChest()) {
				alive++;
				mWinteam = t;
			}
		}
		if (alive != 1) {
			mWinteam = -1;
		}
		// Égalité : on regarde la vie des équipes
		if (mWinteam == -1 && drawCheckLife) {
			if (teams.get(0).getLife() > teams.get(1).getLife()) {
				mWinteam = 0;
			} else if (teams.get(1).getLife() > teams.get(0).getLife()) {
				mWinteam = 1;
			}
		}
	}

	public void init() {

		// Create level/skin list
		JSONObject list = new JSONObject();

		for (Entity l : mEntities.values()) {

			JSONArray data = new JSONArray();
			data.add(l.getLevel());
			data.add(l.getSkin());

			if (l.getHat() > 0) {
				data.add(l.getHat());
			} else {
				data.add(null);
			}
			list.put(String.valueOf(l.getId()), data);
		}
		mLeekDatas = list.toJSONString();

		int obstacle_count = getRandom().getInt(30, 80);

		this.map = Map.generateMap(this, context, 18, 18, obstacle_count, teams, custom_map);

		// Initialize positions and game order
		StartOrder bootorder = new StartOrder();
		this.order = new Order();

		for (Team t : teams) {
			for (Entity e : t.getEntities()) {
				bootorder.addEntity(e);
			}
		}
		for (Entity e : bootorder.compute(this)) {
			this.order.addEntity(e);
			actions.addEntity(e, false);
			initialOrder.add(e);

			// Coffre ?
			if (e.getType() == Entity.TYPE_CHEST) {
				statistics.chest();
			}
		}

		// On ajoute la map
		actions.addMap(map);

		// Cooldowns initiaux
		for (Entry<Integer, Chip> entry : Chips.getTemplates().entrySet()) {
			Chip chip = entry.getValue();
			if (chip.getInitialCooldown() > 0) {
				for (Team t : teams) {
					for (Entity entity : t.getEntities()) {
						addCooldown(entity, chip, chip.getInitialCooldown() + 1);
					}
				}
			}
		}

		// Puis on ajoute le startfight
		actions.log(new ActionStartFight(teams.get(0).size(), teams.get(1).size()));

		this.mState = STATE_RUNNING;
	}

	public void startTurn() throws Exception {

		Entity current = order.current();
		if (current == null) {
			return;
		}

		actions.log(new ActionEntityTurn(current));
		// Log.i(TAG, "Start turn of " + current.getName());

		current.startTurn();

		if (!current.isDead()) {

			// TODO entity turn

			current.endTurn();
			actions.log(new ActionEndTurn(current));
		}
		endTurn();
	}

	public void onPlayerDie(Entity entity, Entity killer) {

		order.removeEntity(entity);
		if (entity.getCell() != null) {
			entity.getCell().setPlayer(null);
		}
		entity.setCell(null);

		actions.log(new ActionEntityDie(entity, killer));
		statistics.kill(killer, entity);

		// BR : give 10 (or 2 for bulb) power + 50% of power to the killer
		if (this.type == State.TYPE_BATTLE_ROYALE && killer != null) {
			int amount = entity.isSummon() ? 2 : 10;
			var effect = entity.getEffects().stream().filter(e -> e.getAttack() == null && e.getID() == Effect.TYPE_RAW_BUFF_POWER).findAny().orElse(null);
			if (effect != null) {
				amount += (int) (effect.value / 2);
			}
			Effect.createEffect(this, Effect.TYPE_RAW_BUFF_POWER, -1, 1, amount, 0, false, killer, killer, null, 0, true, 0, 1, 0, Effect.MODIFIER_IRREDUCTIBLE);
		}

		// Coffre ouvert
		if (entity.getType() == Entity.TYPE_CHEST) {

			if (this.context != State.CONTEXT_CHALLENGE) {
				var resources = entity.loot(this);
				actions.log(new ActionChestOpened(killer, entity, resources));
				statistics.chestKilled(killer, entity, resources);
			}

			int amount = entity.getLevel() == 100 ? 10 : (entity.getLevel() == 200 ? 50 : 100);
			Effect.createEffect(this, Effect.TYPE_RAW_BUFF_POWER, -1, 1, amount, 0, false, killer, killer, null, 0, true, 0, 1, 0, Effect.MODIFIER_IRREDUCTIBLE);
		}

		// Passive effect ally killed
		if (!entity.isSummon()) {
			for (var ally : getTeamEntities(entity.getTeam())) {
				if (ally == entity) continue;
				ally.onAllyKilled();
			}
		}

		// Passive effect kill
		if (killer != null) {
			killer.onKill();
		}

		// Chest kill
		if (killer != null && killer.getType() == Entity.TYPE_CHEST) {
			statistics.chestKill();
		}
	}

	/*
	 * Determines if the fight is over : only one team is alive
	 */
	public boolean isFinished() {

		int aliveTeams = 0;
		for (Team team : teams) {
			if (team.isAlive() && !team.containsChest()) {
				aliveTeams++;
				if (aliveTeams >= 2) {
					return false;
				}
			}
		}
		return true;
	}

	public void endTurn() {

		if (isFinished()) {
			mState = State.STATE_FINISHED;
		} else {

			if (order.next()) {

				if (lastTurn != order.getTurn() && order.getTurn() <= State.MAX_TURNS) {
					actions.log(new ActionNewTurn(order.getTurn()));
					lastTurn = order.getTurn();
					// System.out.println("Turn " + order.getTurn());

					// Battle Royale powers
					if (type == State.TYPE_BATTLE_ROYALE) {
						giveBRPower();
					}
				}

				for (Team t : teams) {
					t.applyCoolDown();
				}
			}
		}
	}

	public void giveBRPower() {
		// X power, infinite duration
		int power = 2;
		for (var entity : getAllEntities(false)) {
			Effect.createEffect(this, Effect.TYPE_RAW_BUFF_POWER, -1, 1, power, 0, false, entity, entity, null, 0, true, 0, 1, 0, Effect.MODIFIER_IRREDUCTIBLE);
		}
	}

	// Attaques; Déplacements...
	public boolean setWeapon(int entity, Weapon weapon) {
		return setWeapon(getEntity(entity), weapon);
	}

	public boolean setWeapon(Entity entity, Weapon weapon) {

		// 1 TP required
		if (entity.getTP() <= 0) return false;

		entity.setWeapon(weapon);
		entity.useTP(1);
		log(new ActionSetWeapon(weapon));
		this.statistics.setWeapon(entity, weapon);

		return true;
	}

	public int useWeapon(int launcher, int target) {
		return useWeapon(getEntity(launcher), getMap().getCell(target));
	}

	public int useWeapon(Entity launcher, Cell target) {

		if (order.current() != launcher || launcher.getWeapon() == null) {
			return Attack.USE_INVALID_TARGET;
		}

		Weapon weapon = launcher.getWeapon();
		if (weapon.getCost() > launcher.getTP()) {
			return Attack.USE_NOT_ENOUGH_TP;
		}

		if (!Pathfinding.canUseAttack(launcher.getCell(), target, weapon.getAttack())) {
			return Attack.USE_INVALID_POSITION;
		}

		boolean critical = generateCritical(launcher);
		int result = critical ? Attack.USE_CRITICAL : Attack.USE_SUCCESS;

		var cellEntity = target.getPlayer();
		ActionUseWeapon log_use = new ActionUseWeapon(target, result);
		actions.log(log_use);
		if (critical) launcher.onCritical();
		List<Entity> target_leeks = weapon.getAttack().applyOnCell(this, launcher, target, critical);
		statistics.useWeapon(launcher, weapon, target, target_leeks, cellEntity);
		if (critical) statistics.critical(launcher);

		launcher.useTP(weapon.getCost());

		return result;
	}

	public int useChip(int caster, int targetCell, Chip template) {

		return useChip(getEntity(caster), getMap().getCell(targetCell), template);
	}

	public int useChip(Entity caster, Cell target, Chip template) {

		if (order.current() != caster) {
			return Attack.USE_INVALID_TARGET;
		}
		if (template.getCost() > caster.getTP()) {
			return Attack.USE_NOT_ENOUGH_TP;
		}
		if (hasCooldown(caster, template)) {
			return Attack.USE_INVALID_COOLDOWN;
		}
		if (!target.isWalkable() || !Pathfinding.canUseAttack(caster.getCell(), target, template.getAttack())) {
			statistics.useInvalidPosition(caster, template.getAttack(), target);
			return Attack.USE_INVALID_POSITION;
		}
		// Invocation mais sans IA
		// TODO
		// if (template.getAttack().getEffectParametersByType(Effect.TYPE_SUMMON) != null) {
		// 	caster.getAI().addSystemLog(LeekLog.WARNING, FarmerLog.BULB_WITHOUT_AI);
		// 	caster.getAI().addSystemLog(LeekLog.STANDARD, Error.HELP_PAGE_LINK, new String[] { "summons" });
		// 	return summonEntity(caster, target, template, null);
		// }

		for (EffectParameters parameters : template.getAttack().getEffects()) {
			// Si c'est une téléportation on ajoute une petite vérification
			if (parameters.getId() == Effect.TYPE_TELEPORT && !target.available()) {
				return Attack.USE_INVALID_TARGET;
			}
			// Impossible d'inverser une entité statique
			if (parameters.getId() == Effect.TYPE_PERMUTATION && target.getPlayer() != null && target.getPlayer().isStatic()) {
				return Attack.USE_INVALID_TARGET;
			}
		}

		boolean critical = generateCritical(caster);
		int result = critical ? Attack.USE_CRITICAL : Attack.USE_SUCCESS;

		var cellEntity = target.getPlayer();
		ActionUseChip log = new ActionUseChip(target, template, result);
		actions.log(log);
		if (critical) caster.onCritical();
		List<Entity> targets = template.getAttack().applyOnCell(this, caster, target, critical);
		statistics.useChip(caster, template, target, targets, cellEntity);
		if (critical) statistics.critical(caster);

		if (template.getCooldown() != 0) {
			addCooldown(caster, template);
		}

		caster.useTP(template.getCost());

		return result;
	}

	public int moveEntity(Entity entity, List<Cell> path) {

		if (entity.isStatic()) return 0; // Static entity cannot move.

		int size = path.size();
		if (size == 0) {
			return 0;
		}
		if (size > entity.getMP()) {
			return 0;
		}

		actions.log(new ActionMove(entity, path));
		statistics.move(entity, entity, entity.getCell(), path);

		entity.useMP(size);
		entity.getCell().setPlayer(null);
		entity.setCell(path.get(path.size() - 1));
		entity.getCell().setPlayer(entity);

		return path.size();
	}

	public void moveEntity(Entity entity, Cell cell) {

		if (entity.isStatic()) return; // Static entity cannot move.

		entity.getCell().setPlayer(null);
		entity.setCell(cell);
		entity.getCell().setPlayer(entity);
	}

	public void teleportEntity(Entity entity, Cell cell, Entity caster) {

		Cell start = entity.getCell();
		entity.setCell(null);

		start.setPlayer(null);
		cell.setPlayer(entity);

		statistics.move(caster, entity, start, new ArrayList<>(Arrays.asList(cell)));

		if (start != cell) {
			entity.onMoved(caster);
		}

		statistics.teleportation(entity, caster, start, cell);
	}

	public void slideEntity(Entity entity, Cell cell, Entity caster) {

		if (entity.isStatic()) return;

		Cell start = entity.getCell();

		if (cell != start) {
			entity.setCell(null);

			start.setPlayer(null);
			cell.setPlayer(entity);

			statistics.move(caster, entity, start, Pathfinding.getAStarPath(start, new Cell[] { cell }, Arrays.asList(cell, start)));
			statistics.slide(entity, caster, start, cell);
			entity.onMoved(caster);
		}
	}

	public void invertEntities(Entity caster, Entity target) {

		if (target.isStatic()) return;

		Cell start = caster.getCell();
		Cell end = target.getCell();
		if (start == null || end == null) {
			return;
		}

		target.setCell(start);
		start.setCellPlayer(target);

		end.setCellPlayer(caster);
		caster.setCell(end);

		statistics.move(caster, caster, start, new ArrayList<>(Arrays.asList(end)));
		statistics.move(caster, target, end, new ArrayList<>(Arrays.asList(start)));

		// Passifs
		target.onMoved(caster);
		caster.onMoved(caster);
	}

	public int summonEntity(Entity caster, Cell target, Chip template) {

		EffectParameters params = template.getAttack().getEffectParametersByType(Effect.TYPE_SUMMON);
		if (order.current() != caster || params == null) {
			return -1;
		}
		if (template.getCost() > caster.getTP()) {
			return -2;
		}
		if (hasCooldown(caster, template)) {
			return -3;
		}
		if (!Pathfinding.canUseAttack(caster.getCell(), target, template.getAttack())) {
			return -4;
		}
		if (!target.available()) {
			return -4;
		}
		if (teams.get(caster.getTeam()).getSummonCount() >= SUMMON_LIMIT) {
			return -5;
		}

		boolean critical = generateCritical(caster);
		int result = critical ? Attack.USE_CRITICAL : Attack.USE_SUCCESS;

		ActionUseChip log = new ActionUseChip(target, template, result);
		actions.log(log);
		if (critical) caster.onCritical();

		// On invoque
		Entity summon = createSummon(caster, (int) params.getValue1(), target, template.getLevel(), critical);

		// On balance l'action
		actions.log(new ActionInvocation(summon, result));
		statistics.summon(caster, summon);
		statistics.useChip(caster, template, target, new ArrayList<>(), null);

		if (template.getCooldown() != 0) {
			addCooldown(caster, template);
		}

		caster.useTP(template.getCost());

		return result;
	}

	public int resurrectEntity(Entity caster, Cell target, Chip template, Entity target_entity) {

		if (order.current() != caster) {
			return Attack.USE_INVALID_TARGET;
		}
		if (template.getCost() > caster.getTP()) {
			return Attack.USE_NOT_ENOUGH_TP;
		}
		if (!Pathfinding.canUseAttack(caster.getCell(), target, template.getAttack())) {
			return Attack.USE_INVALID_POSITION;
		}
		if (hasCooldown(caster, template)) {
			return Attack.USE_INVALID_COOLDOWN;
		}
		EffectParameters params = template.getAttack().getEffectParametersByType(Effect.TYPE_RESURRECT);
		if (params == null || !target.available() || !target_entity.isDead() || target_entity.getTeam() != caster.getTeam()) {
			return Attack.USE_INVALID_TARGET;
		}

		if (target_entity.isSummon()) {
			// It's a summon
			if (teams.get(target_entity.getTeam()).getSummonCount() >= SUMMON_LIMIT) {
				return Attack.USE_TOO_MANY_SUMMONS;
			}
		}

		boolean critical = generateCritical(caster);
		int result = critical ? Attack.USE_CRITICAL : Attack.USE_SUCCESS;

		ActionUseChip log = new ActionUseChip(target, template, result);
		actions.log(log);
		if (critical) caster.onCritical();

		// Resurrect
		resurrect(caster, target_entity, target, critical);
		statistics.useChip(caster, template, target, new ArrayList<>(), null);
		statistics.resurrect(caster, target_entity);

		if (template.getCooldown() != 0) {
			addCooldown(caster, template);
		}

		caster.useTP(template.getCost());

		return result;
	}

	public boolean generateCritical(Entity caster) {
		return getRandom().getDouble() < ((double) caster.getAgility() / 1000);
	}

	public Bulb createSummon(Entity owner, int type, Cell target, int level, boolean critical) {

		int fid = getNextEntityId();
		Bulb invoc = Bulb.create(owner, -fid, type, level, critical);
		invoc.setState(this, fid);

		int team = owner.getTeam();

		invoc.setTeam(team);
		teams.get(team).addEntity(invoc);

		// On ajoute dans les tableaux
		mEntities.put(invoc.getFId(), invoc);

		// On ajoute dans l'ordre de jeu
		order.addSummon(owner, invoc);

		// On met la cellule
		target.setPlayer(invoc);

		// On l'ajoute dans les infos du combat
		actions.addEntity(invoc, critical);

		return invoc;
	}

	public void removeInvocation(Entity invoc, boolean force) {

		// Mort d'une invocation, on la retire des listes
		teams.get(invoc.getTeam()).removeEntity(invoc);

		if (force) {
			mEntities.remove(invoc.getFId());
		}
	}

	public void resurrect(Entity owner, Entity entity, Cell cell, boolean critical) {

		Entity next = null;
		boolean start = false;
		for (Entity e : initialOrder) {
			if (e == entity) {
				start = true;
				continue;
			}
			if (!start) {
				continue;
			}
			if (e.isDead()) {
				continue;
			}
			next = e;
			break;
		}
		if (next == null) {
			order.addEntity(entity);
		} else {
			order.addEntity(order.getEntityTurnOrder(next) - 1, entity);
		}
		entity.resurrect(owner, critical ? Effect.CRITICAL_FACTOR : 1.0);

		// On met la cellule
		cell.setPlayer(entity);

		// On balance l'action
		actions.log(new ActionResurrect(owner, entity));
	}

	public int getTurn() {
		return order.getTurn();
	}

	public Order getOrder() {
		return order;
	}

	public Actions getActions() {
		return actions;
	}

	public int getFullType() {
		return fullType;
	}

	public void setId(int f) {
		mId = f;
	}

	public java.util.Map<Integer, Leek> getLeeks() {
		java.util.Map<Integer, Leek> retour = new TreeMap<Integer, Leek>();
		for (Entry<Integer, Entity> e : mEntities.entrySet()) {
			Leek l = e.getValue().getLeek();
			if (l != null)
				retour.put(e.getKey(), l);
		}
		return retour;
	}

	public int getNextEntityId() {
		int id = mNextEntityId;
		mNextEntityId++;
		return id;
	}

	// Add a cooldown for a chip
	public void addCooldown(Entity entity, Chip chip) {
		addCooldown(entity, chip, chip.getCooldown());
	}

	public void addCooldown(Entity entity, Chip chip, int cooldown) {
		if (chip == null) {
			return;
		}
		if (chip.isTeamCooldown()) {
			teams.get(entity.getTeam()).addCooldown(chip, cooldown);
		} else {
			entity.addCooldown(chip, cooldown);
		}
	}

	// Chip has cooldown?
	public boolean hasCooldown(Entity entity, Chip chip) {
		if (chip == null) {
			return false;
		}
		if (chip.isTeamCooldown()) {
			return teams.get(entity.getTeam()).hasCooldown(chip.getId());
		} else {
			return entity.hasCooldown(chip.getId());
		}
	}

	// Get current cooldown of a chip
	public int getCooldown(Entity entity, Chip chip) {
		if (chip == null) {
			return 0;
		}
		if (chip.isTeamCooldown()) {
			return teams.get(entity.getTeam()).getCooldown(chip.getId());
		} else {
			return entity.getCooldown(chip.getId());
		}
	}

	public int getType() {
		return type;
	}

	public int getContext() {
		return context;
	}

	public static int getFightContext(int type) {
		if (type == TYPE_SOLO_GARDEN || type == TYPE_TEAM_GARDEN || type == TYPE_FARMER_GARDEN) {
			return CONTEXT_GARDEN;
		} else if (type == TYPE_SOLO_TEST || type == TYPE_TEAM_TEST || type == TYPE_FARMER_TEST) {
			return CONTEXT_TEST;
		} else if (type == TYPE_TEAM_TOURNAMENT || type == TYPE_SOLO_TOURNAMENT || type == TYPE_FARMER_TOURNAMENT) {
			return CONTEXT_TOURNAMENT;
		} else if (type == FULL_TYPE_BATTLE_ROYALE) {
			return CONTEXT_BATTLE_ROYALE;
		}
		return CONTEXT_CHALLENGE;
	}

	public static int getFightType(int type) {
		if (type == TYPE_SOLO_GARDEN || type == TYPE_SOLO_CHALLENGE || type == TYPE_SOLO_TOURNAMENT || type == TYPE_SOLO_TEST) {
			return State.TYPE_SOLO;
		} else if (type == TYPE_FARMER_GARDEN || type == TYPE_FARMER_TOURNAMENT || type == TYPE_FARMER_CHALLENGE || type == TYPE_FARMER_TEST) {
			return State.TYPE_FARMER;
		} else if (type == FULL_TYPE_BATTLE_ROYALE) {
			return State.TYPE_BATTLE_ROYALE;
		}
		return State.TYPE_TEAM;
	}

	public static boolean isTeamAIFight(int type) {
		return getFightType(type) != TYPE_SOLO;
	}

	public static boolean isTestFight(int type) {
		return getFightContext(type) == CONTEXT_TEST;
	}

	public static boolean isChallenge(int type) {
		return getFightContext(type) == CONTEXT_CHALLENGE;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public JSONObject getDeadReport() {
		JSONObject dead = new JSONObject();
		for (Team team : teams) {
			for (Entity entity : team.getEntities()) {
				dead.put(String.valueOf(entity.getId()), entity.isDead());
			}
		}
		return dead;
	}

	public void setContext(int context) {
		this.context = context;
	}
	public void setType(int type) {
		this.type = type;
	}

	private double getMaxDeadRatio() {

		if (this.type == TYPE_BATTLE_ROYALE) {
			double dead = 0;
			for (Team team : teams) {
				if (team.isDead()) {
					dead++;
				}
			}
			return dead / teams.size();
		}

		double max = 0;
		for (Team team : teams) {
			double ratio = team.getDeadRatio();
			if (ratio > max) max = ratio;
		}
		return max;
	}
	private double getMaxLifeRatio() {

		if (this.type == TYPE_BATTLE_ROYALE) {
			return 0; // For BR, don't count this value
		}

		double max = 0;
		for (Team team : teams) {
			double ratio = 1 - team.getLifeRatio();
			if (ratio > max) max = ratio;
		}
		return max;
	}

	public double getProgress() {
		if (this.order == null) return 0;

		int entityCount = this.order.getEntities().size();

		// Nombre d'entités mortes
		double d = getMaxDeadRatio();
		// Nombre de "tours d'entité", racine carrée
		double t = Math.min(1, Math.pow((double) (this.getTurn() * entityCount + this.order.getPosition()) / (MAX_TURNS * entityCount), 0.5));
		// Ratio de vie restante
		double l = getMaxLifeRatio();

		return Math.max(t, Math.max(d, l));
	}

	public void seed(int seed) {
		this.seed = seed;
		randomGenerator.seed(seed);
	}
	public RandomGenerator getRandom() {
		return randomGenerator;
	}

	public void setRegisterManager(RegisterManager registerManager) {
		this.registerManager = registerManager;
	}

	public RegisterManager getRegisterManager() {
		return this.registerManager;
	}

	public Date getDate() {
		return date;
	}

	public void setStatisticsManager(StatisticsManager statisticsManager) {
		this.statistics = statisticsManager;
	}

	public int getDuration() {
		return order.getTurn();
	}

	public Entity getLastEntity() {
		return mEntities.get(mNextEntityId - 1);
	}

	public int getState() {
		return mState;
	}

	public void setState(int state) {
		mState = state;
	}

	public long getSeed() {
		return this.seed;
	}

	@Override
	public String toString() {
		return "State [\n\t" +
			String.join("\n\t", this.mEntities.values().stream().map(entity -> {
				return entity.getName()
					+ " life=" + entity.getLife() + "/" + entity.getTotalLife()
					+ " str=" + entity.getStrength()
					+ " tp=" + entity.getTP()
					+ " mp=" + entity.getMP()
					+ " rel_sh=" + entity.getRelativeShield() + "%"
					+ " abs_sh=" + entity.getAbsoluteShield()
					+ " pos=" + entity.getCell()
					+ " armor=" + entity.getCooldown(22)
					+ " fortress=" + entity.getCooldown(29)
					;
			}).collect(Collectors.toList()))
		+ "\n]";
	}

	public long moveToward(int entity, long leek_id, long pm_to_use) {
		return moveToward(getEntity(entity), leek_id, pm_to_use);
	}

	public long moveToward(Entity entity, long leek_id, long pm_to_use) {

		int pm = pm_to_use == -1 ? entity.getMP() : (int) pm_to_use;
		if (pm > entity.getMP()) {
			pm = entity.getMP();
		}
		long used_pm = 0;
		if (pm > 0) {
			var target = getEntity(leek_id);
			if (target != null && !target.isDead()) {
				var path = getMap().getPathBeetween(entity.getCell(), target.getCell(), null);
				if (path != null) {
					used_pm = moveEntity(entity, path.subList(0, Math.min(path.size(), pm)));
				}
			}
		}
		return used_pm;
	}

	public long moveTowardCell(int entity, long cell_id, long pm_to_use) {
		return moveTowardCell(getEntity(entity), cell_id, pm_to_use);
	}

	public long moveTowardCell(Entity entity, long cell_id, long pm_to_use) {

		int pm = pm_to_use == -1 ? entity.getMP() : (int) pm_to_use;
		if (pm > entity.getMP()) {
			pm = entity.getMP();
		}
		long used_pm = 0;
		if (pm > 0 && entity.getCell() != null) {
			Cell target = entity.getCell().getMap().getCell((int) cell_id);
			if (target != null && target != entity.getCell()) {
				List<Cell> path = null;
				if (!target.isWalkable())
					path = Pathfinding.getAStarPath(entity.getCell(), Pathfinding.getValidCellsAroundObstacle(target), null);
				else
					path = getMap().getPathBeetween(entity.getCell(), target, null);

				if (path != null) {
					used_pm = moveEntity(entity, path.subList(0, Math.min(pm, path.size())));
				}
			}
		}
		return used_pm;
	}

	public void setBestItem(int item_id) {
		this.bestItem = item_id;
	}

	public int getBestItem() {
		return this.bestItem;
	}
}
